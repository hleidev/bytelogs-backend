package top.harrylei.community.service.comment.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.harrylei.community.api.enums.response.ResultCode;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.api.enums.article.ContentTypeEnum;
import top.harrylei.community.api.enums.notify.NotifyTypeEnum;
import top.harrylei.community.api.enums.rank.ActivityActionEnum;
import top.harrylei.community.api.enums.rank.ActivityTargetEnum;
import top.harrylei.community.api.enums.user.OperateTypeEnum;
import top.harrylei.community.api.enums.user.PraiseStatusEnum;
import top.harrylei.community.api.model.comment.dto.CommentDTO;
import top.harrylei.community.api.model.comment.req.CommentMyQueryParam;
import top.harrylei.community.api.model.comment.req.CommentQueryParam;
import top.harrylei.community.api.model.comment.vo.BaseCommentVO;
import top.harrylei.community.api.model.comment.vo.CommentMyVO;
import top.harrylei.community.api.model.comment.vo.SubCommentVO;
import top.harrylei.community.api.model.comment.vo.TopCommentVO;
import top.harrylei.community.api.model.page.PageVO;
import top.harrylei.community.api.model.user.dto.UserFootDTO;
import top.harrylei.community.api.model.user.dto.UserInfoDTO;
import top.harrylei.community.core.context.ReqInfoContext;
import top.harrylei.community.core.util.KafkaEventPublisher;
import top.harrylei.community.core.util.NumUtil;
import top.harrylei.community.core.util.PageUtils;
import top.harrylei.community.service.article.repository.dao.ArticleDetailDAO;
import top.harrylei.community.service.article.repository.entity.ArticleDO;
import top.harrylei.community.service.article.service.ArticleQueryService;
import top.harrylei.community.service.comment.converted.CommentStructMapper;
import top.harrylei.community.service.comment.repository.dao.CommentDAO;
import top.harrylei.community.service.comment.repository.entity.CommentDO;
import top.harrylei.community.service.comment.service.CommentService;
import top.harrylei.community.service.user.service.UserFootService;
import top.harrylei.community.service.user.service.cache.UserCacheService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentDAO commentDAO;
    private final CommentStructMapper commentStructMapper;
    private final ArticleQueryService articleQueryService;
    private final ArticleDetailDAO articleDetailDAO;
    private final UserFootService userFootService;
    private final UserCacheService userCacheService;
    private final KafkaEventPublisher kafkaEventPublisher;

    /**
     * 保存评论
     *
     * @param dto 评论DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveComment(CommentDTO dto) {
        CommentDO comment = insertComment(dto);
        log.info("评论保存成功 commentId={}", comment.getId());
        return comment.getId();
    }

    /**
     * 分页查询
     *
     * @param param 分页查询参数
     * @return 分页结果
     */
    @Override
    public PageVO<TopCommentVO> pageQuery(CommentQueryParam param) {
        // 执行分页查询并转换数据
        IPage<CommentDO> comments = commentDAO.pageQuery(param.getArticleId(), PageUtils.of(param));

        return PageUtils.fromList(comments, records -> buildTopCommentsWithSub(records, param.getArticleId()));
    }

    /**
     * 编辑评论
     *
     * @param dto 编辑数据传输对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateComment(CommentDTO dto) {
        // 1. 验证评论是否存在并获取
        CommentDO comment = getCommentById(dto.getId());

        // 2. 验证评论可见性
        validateCommentVisibility(comment);

        // 3. 验证编辑权限
        validateCommentEditPermission(comment);

        // 4. 更新评论内容
        comment.setContent(dto.getContent());
        commentDAO.updateById(comment);

        log.info("评论编辑成功 commentId={}", comment.getId());
    }

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        updateCommentDeletedStatus(commentId, DeleteStatusEnum.DELETED);
    }

    /**
     * 恢复评论
     *
     * @param commentId 评论ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreComment(Long commentId) {
        updateCommentDeletedStatus(commentId, DeleteStatusEnum.NOT_DELETED);
    }

    /**
     * 查询用户评论
     *
     * @param userId 用户ID
     * @param param  分页查询参数
     * @return 分页结果
     */
    @Override
    public PageVO<CommentMyVO> pageQueryUserComments(Long userId, CommentMyQueryParam param) {
        // 执行分页查询并转换数据
        IPage<CommentDO> comments = commentDAO.pageQueryUserComments(userId, PageUtils.of(param));

        return PageUtils.fromList(comments, this::buildMyComments);
    }

    /**
     * 评论操作
     *
     * @param commentId 评论ID
     * @param type      操作类型
     */
    @Override
    public void actionComment(Long commentId, OperateTypeEnum type) {
        CommentDO comment = getCommentById(commentId);
        if (comment == null) {
            ResultCode.COMMENT_NOT_EXISTS.throwException();
        }

        Long currentUserId = ReqInfoContext.getContext().getUserId();

        Boolean success = userFootService.actionComment(currentUserId, type, comment.getUserId(), commentId);
        if (!success) {
            ResultCode.INTERNAL_ERROR.throwException();
        }

        log.info("评论{}操作成功 commentId={} type={}", type.getLabel(), commentId, type);
    }

    /**
     * 构建顶级评论列表
     */
    private List<TopCommentVO> buildTopCommentsWithSub(List<CommentDO> comments, Long articleId) {
        // 顶级评论Map
        Map<Long, TopCommentVO> topCommentsMap = comments.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(CommentDO::getId, commentStructMapper::toTopVO));

        // 查询子评论列表
        List<CommentDO> subComments = commentDAO.listSubComments(articleId, topCommentsMap.keySet());
        Map<Long, SubCommentVO> subCommentsMap = subComments.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(CommentDO::getId, commentStructMapper::toSubVO));

        // 构建顶级评论和子评论的关系
        subComments.forEach(comment -> {
            TopCommentVO topComment = topCommentsMap.get(comment.getTopCommentId());
            SubCommentVO subComment = subCommentsMap.get(comment.getId());

            if (topComment != null && subComment != null) {
                topComment.getChildComments().add(subComment);

                if (Objects.equals(comment.getTopCommentId(), comment.getParentCommentId())) {
                    return;
                }
                SubCommentVO parentComment = subCommentsMap.get(comment.getParentCommentId());
                if (parentComment != null) {
                    subComment.setParentContent(parentComment.getContent());
                }
            }
        });

        // 填充用户信息
        List<TopCommentVO> result = new ArrayList<>(topCommentsMap.size());
        comments.forEach(comment -> {
            TopCommentVO topComment = topCommentsMap.get(comment.getId());
            fillCommentInfo(topComment);
            topComment.getChildComments().forEach(this::fillCommentInfo);
            // 设置评论数量为子评论数量
            topComment.setCommentCount(topComment.getChildComments().size());
            result.add(topComment);
        });

        return result;
    }

    private void fillCommentInfo(BaseCommentVO comment) {
        // 填充用户信息
        UserInfoDTO userInfo = userCacheService.getUserInfo(comment.getUserId());
        if (userInfo == null) {
            comment.setUserName("默认用户");
            comment.setUserAvatar("");
        } else {
            comment.setUserName(userInfo.getUserName());
            comment.setUserAvatar(userInfo.getAvatar());
        }

        // 填充点赞信息

        // TODO 待完成，查询并设置点赞数

        Long loginUserId = null;
        try {
            loginUserId = ReqInfoContext.getContext().getUserId();
        } catch (Exception e) {
            log.debug("获取用户上下文失败", e);
        }

        if (loginUserId != null) {
            // 判断当前用户是否点过赞
            UserFootDTO userFoot = userFootService.getUserFoot(loginUserId, comment.getId(), ContentTypeEnum.COMMENT);
            comment.setPraised(userFoot != null && PraiseStatusEnum.PRAISE.equals(userFoot.getPraiseState()));
        } else {
            comment.setPraised(false);
        }
    }

    /**
     * 插入评论
     */
    private CommentDO insertComment(CommentDTO dto) {
        // 验证文章是否存在
        ArticleDO article = articleQueryService.getArticleById(dto.getArticleId());
        if (article == null) {
            ResultCode.ARTICLE_NOT_EXISTS.throwException();
        }

        // 验证父评论是否存在
        CommentDO parent = getParentComment(dto.getParentCommentId());

        // 保存评论
        CommentDO comment = commentStructMapper.toDO(dto);

        // 设置顶级评论ID
        if (parent == null) {
            comment.setTopCommentId(0L);
        } else {
            // 子评论，设置顶级评论ID
            Long topCommentId = parent.getTopCommentId();
            if (NumUtil.nullOrZero(topCommentId)) {
                // 父评论本身就是顶级评论
                comment.setTopCommentId(parent.getId());
            } else {
                // 父评论是子评论，继承其顶级评论ID
                comment.setTopCommentId(topCommentId);
            }
        }

        commentDAO.save(comment);

        // 保存用户足迹
        Long parentUserId = parent != null ? parent.getUserId() : null;
        userFootService.saveCommentFoot(comment, article.getUserId(), parentUserId);

        // 发布通知事件
        publishCommentNotificationEvent(comment, article, parent);

        // 发布活跃度事件
        publishCommentActivityEvent(comment, ActivityActionEnum.COMMENT);

        return comment;
    }

    /**
     * 通用的足迹处理方法
     *
     * @param comment  评论对象
     * @param isDelete 是否为删除操作（true: 删除足迹, false: 创建足迹）
     */
    private void updateCommentFoot(CommentDO comment, boolean isDelete) {
        try {
            // 获取文章信息
            ArticleDO article = articleQueryService.getArticleById(comment.getArticleId());

            // 获取父评论作者ID
            Long parentUserId = null;
            if (!NumUtil.nullOrZero(comment.getParentCommentId())) {
                CommentDO parent = commentDAO.getById(comment.getParentCommentId());
                if (parent != null) {
                    parentUserId = parent.getUserId();
                }
            }

            // 执行足迹操作
            if (isDelete) {
                userFootService.deleteCommentFoot(comment, article.getUserId(), parentUserId);
            } else {
                userFootService.saveCommentFoot(comment, article.getUserId(), parentUserId);
            }
        } catch (Exception e) {
            String action = isDelete ? "删除" : "恢复";
            log.error("{}评论用户足迹失败，commentId={}", action, comment.getId(), e);
        }
    }

    /**
     * 根据ID获取评论，不存在时抛出异常
     *
     * @param commentId 评论ID
     * @return 评论实体
     */
    private CommentDO getCommentById(Long commentId) {
        CommentDO comment = commentDAO.getById(commentId);
        if (comment == null) {
            ResultCode.COMMENT_NOT_EXISTS.throwException();
        }
        return comment;
    }

    /**
     * 验证评论可见性（管理员可以操作已删除评论）
     *
     * @param comment 评论实体
     */
    private void validateCommentVisibility(CommentDO comment) {
        boolean isAdmin = ReqInfoContext.getContext().isAdmin();
        boolean isDeleted = DeleteStatusEnum.DELETED.equals(comment.getDeleted());

        // 管理员可以操作已删除评论，普通用户不能操作已删除评论
        if (isDeleted && !isAdmin) {
            ResultCode.COMMENT_NOT_EXISTS.throwException();
        }
    }

    /**
     * 验证评论作者权限
     *
     * @param authorId 评论作者ID
     */
    private void validateCommentAuthorPermission(Long authorId) {
        boolean isAdmin = ReqInfoContext.getContext().isAdmin();
        boolean isAuthor = Objects.equals(authorId, ReqInfoContext.getContext().getUserId());
        if (!isAdmin && !isAuthor) {
            ResultCode.FORBIDDEN.throwException();
        }
    }


    /**
     * 验证评论编辑权限
     *
     * @param comment 评论实体
     */
    private void validateCommentEditPermission(CommentDO comment) {
        // 验证是否为评论作者
        validateCommentAuthorPermission(comment.getUserId());

        // 验证编辑时间窗口（24小时内可编辑）
        Duration timeDiff = Duration.between(comment.getCreateTime(), LocalDateTime.now());
        if (timeDiff.toHours() > 24) {
            ResultCode.OPERATION_NOT_ALLOWED.throwException();
        }
    }

    /**
     * 通用的评论状态更新方法
     *
     * @param commentId    评论ID
     * @param targetStatus 目标删除状态
     */
    private void updateCommentDeletedStatus(Long commentId, DeleteStatusEnum targetStatus) {
        // 1. 验证评论是否存在并获取
        CommentDO comment = getCommentById(commentId);
        DeleteStatusEnum oldStatus = comment.getDeleted();

        // 2. 验证权限
        validateCommentAuthorPermission(comment.getUserId());

        // 3. 检查并更新状态
        if (checkAndUpdateCommentStatus(comment, targetStatus)) {
            // 4. 处理用户足迹
            boolean isDelete = targetStatus == DeleteStatusEnum.DELETED;
            updateCommentFoot(comment, isDelete);

            // 5. 发布活跃度事件
            ActivityActionEnum actionType = isDelete ? ActivityActionEnum.DELETE_COMMENT : ActivityActionEnum.COMMENT;
            publishCommentActivityEvent(comment, actionType);

            log.info("评论状态更新成功，commentId={}, deleted={}", commentId, targetStatus);
        } else {
            log.info("评论状态无需更新，commentId={}, deleted={}", commentId, oldStatus);
        }
    }

    /**
     * 检查并更新评论状态
     *
     * @param comment      评论DO对象
     * @param targetStatus 目标状态
     * @return 是否执行了更新操作
     */
    private boolean checkAndUpdateCommentStatus(CommentDO comment, DeleteStatusEnum targetStatus) {
        // 检查状态是否需要更新
        if (Objects.equals(comment.getDeleted(), targetStatus)) {
            return false;
        }

        // 执行状态更新
        comment.setDeleted(targetStatus);
        commentDAO.updateById(comment);
        return true;
    }

    private CommentDO getParentComment(Long parentCommentId) {
        if (NumUtil.nullOrZero(parentCommentId)) {
            return null;
        }

        CommentDO parent = commentDAO.getById(parentCommentId);

        if (parent == null) {
            ResultCode.COMMENT_NOT_EXISTS.throwException();
        }
        return parent;
    }

    /**
     * 构建用户评论列表
     */
    private List<CommentMyVO> buildMyComments(List<CommentDO> comments) {
        List<CommentMyVO> result = new ArrayList<>(comments.size());

        // 转换为CommentMyVO
        for (CommentDO comment : comments) {
            CommentMyVO commentMy = commentStructMapper.toMyVO(comment);

            // 填充文章信息
            try {
                String articleTitle = articleDetailDAO.getPublishedTitle(comment.getArticleId());
                commentMy.setArticleTitle(Optional.ofNullable(articleTitle).orElse("文章未发布或已删除"));
            } catch (Exception e) {
                log.warn("获取文章信息失败, articleId={}", comment.getArticleId(), e);
                commentMy.setArticleTitle("文章已删除");
            }

            // 填充父评论内容
            if (!NumUtil.nullOrZero(comment.getParentCommentId())) {
                try {
                    CommentDO parentComment = commentDAO.getById(comment.getParentCommentId());
                    if (parentComment != null && !DeleteStatusEnum.DELETED.equals(parentComment.getDeleted())) {
                        commentMy.setParentContent(parentComment.getContent());
                    }
                    // 父评论已删除时，保持parentContent为null，由前端处理显示
                } catch (Exception e) {
                    log.warn("获取父评论信息失败, parentCommentId={}", comment.getParentCommentId(), e);
                }
            }

            result.add(commentMy);
        }

        return result;
    }


    /**
     * 发布评论通知事件
     *
     * @param comment 评论对象
     * @param article 文章对象
     * @param parent  父评论对象（如果是回复）
     */
    private void publishCommentNotificationEvent(CommentDO comment, ArticleDO article, CommentDO parent) {
        try {
            // 1. 对文章的评论通知
            if (!comment.getUserId().equals(article.getUserId())) {
                kafkaEventPublisher.publishUserBehaviorEvent(comment.getUserId(),
                        article.getUserId(), article.getId(), ContentTypeEnum.ARTICLE, NotifyTypeEnum.COMMENT);
                log.debug("发布评论文章通知事件: commentUserId={}, articleAuthorId={}, articleId={}",
                        comment.getUserId(), article.getUserId(), article.getId());
            }

            // 2. 对父评论的回复通知
            if (parent != null && !comment.getUserId().equals(parent.getUserId())) {
                kafkaEventPublisher.publishUserBehaviorEvent(comment.getUserId(),
                        parent.getUserId(), parent.getId(), ContentTypeEnum.COMMENT, NotifyTypeEnum.REPLY);
                log.debug("发布回复评论通知事件: replyUserId={}, parentCommentAuthorId={}, parentCommentId={}",
                        comment.getUserId(), parent.getUserId(), parent.getId());
            }

        } catch (Exception e) {
            // 事件发布失败不影响主业务流程
            log.error("发布评论通知事件失败: commentId={}, articleId={}", comment.getId(), article.getId(), e);
        }
    }

    /**
     * 发布评论活跃度事件
     *
     * @param comment    评论对象
     * @param actionType 行为类型
     */
    private void publishCommentActivityEvent(CommentDO comment, ActivityActionEnum actionType) {
        try {
            // 发布评论活跃度事件，用于积分计算
            kafkaEventPublisher.publishUserActivityEvent(comment.getUserId(),
                    comment.getArticleId(), ActivityTargetEnum.ARTICLE, actionType);

            log.debug("发布评论活跃度事件: userId={}, articleId={}, action={}",
                    comment.getUserId(), comment.getArticleId(), actionType.getLabel());

        } catch (Exception e) {
            // 事件发布失败不影响主业务流程
            log.error("发布评论活跃度事件失败: commentId={}, articleId={}, userId={}, action={}",
                    comment.getId(), comment.getArticleId(), comment.getUserId(), actionType.getLabel(), e);
        }
    }
}