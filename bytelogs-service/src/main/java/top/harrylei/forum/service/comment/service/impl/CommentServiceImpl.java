package top.harrylei.forum.service.comment.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.harrylei.forum.api.model.enums.ErrorCodeEnum;
import top.harrylei.forum.api.model.enums.PraiseStatusEnum;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.enums.comment.ContentTypeEnum;
import top.harrylei.forum.api.model.vo.comment.dto.CommentDTO;
import top.harrylei.forum.api.model.vo.comment.vo.BaseCommentVO;
import top.harrylei.forum.api.model.vo.comment.vo.SubCommentVO;
import top.harrylei.forum.api.model.vo.comment.vo.TopCommentVO;
import top.harrylei.forum.api.model.vo.comment.vo.CommentMyVO;
import top.harrylei.forum.api.model.vo.comment.req.CommentQueryParam;
import top.harrylei.forum.api.model.vo.comment.req.CommentMyQueryParam;
import top.harrylei.forum.api.model.vo.page.PageHelper;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.api.model.vo.user.dto.UserFootDTO;
import top.harrylei.forum.api.model.vo.user.dto.UserInfoDetailDTO;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.core.util.NumUtil;
import top.harrylei.forum.service.article.repository.entity.ArticleDO;
import top.harrylei.forum.service.article.service.ArticleService;
import top.harrylei.forum.service.comment.converted.CommentStructMapper;
import top.harrylei.forum.service.comment.repository.dao.CommentDAO;
import top.harrylei.forum.service.comment.repository.entity.CommentDO;
import top.harrylei.forum.service.comment.service.CommentService;
import top.harrylei.forum.service.user.service.UserFootService;
import top.harrylei.forum.service.user.service.cache.UserCacheService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
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
    private final ArticleService articleService;
    private final UserFootService userFootService;
    private final UserCacheService userCacheService;

    /**
     * 保存评论
     *
     * @param dto 评论DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveComment(CommentDTO dto) {
        CommentDO comment = insertComment(dto);
        log.info("评论保存成功，commentId={}", comment.getId());
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
        return executePageQuery(
                () -> commentDAO.pageQuery(param.getArticleId(), new Page<>(param.getPageNum(), param.getPageSize())),
                records -> buildTopCommentsWithSub(records, param.getArticleId())
        );
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

        log.info("评论编辑成功，commentId={}", comment.getId());
    }

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        // 1. 验证评论是否存在并获取
        CommentDO comment = getCommentById(commentId);

        // 2. 验证删除权限
        validateCommentAuthorPermission(comment.getUserId());

        // 3. 检查并删除评论
        if (checkAndDeleteComment(comment)) {
            log.info("评论删除成功，commentId={}", commentId);
        } else {
            log.info("评论已处于删除状态，无需重复操作，commentId={}", commentId);
        }
    }

    /**
     * 查询用户评论
     *
     * @param userId 用户ID
     * @param param  分页查询参数
     * @return 分页结果
     */
    @Override
    public PageVO<CommentMyVO> queryUserComments(Long userId, CommentMyQueryParam param) {
        return executePageQuery(
                () -> commentDAO.pageQueryUserComments(
                        userId,
                        new Page<>(param.getPageNum(), param.getPageSize())),
                this::buildMyComments
        );
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
        UserInfoDetailDTO userInfo = userCacheService.getUserInfo(comment.getUserId());
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
        ArticleDO article = articleService.getArticleById(dto.getArticleId());
        ExceptionUtil.requireValid(article, ErrorCodeEnum.ARTICLE_NOT_EXISTS, "articleId=" + dto.getArticleId());

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

        return comment;
    }

    /**
     * 根据ID获取评论，不存在时抛出异常
     *
     * @param commentId 评论ID
     * @return 评论实体
     */
    private CommentDO getCommentById(Long commentId) {
        CommentDO comment = commentDAO.getById(commentId);
        ExceptionUtil.requireValid(comment, ErrorCodeEnum.COMMENT_NOT_EXISTS, "评论不存在，commentId=" + commentId);
        return comment;
    }

    /**
     * 验证评论可见性（管理员可以操作已删除评论）
     *
     * @param comment 评论实体
     */
    private void validateCommentVisibility(CommentDO comment) {
        boolean isAdmin = ReqInfoContext.getContext().isAdmin();
        boolean isDeleted = Objects.equals(comment.getDeleted(), YesOrNoEnum.YES.getCode());

        // 管理员可以操作已删除评论，普通用户不能操作已删除评论
        ExceptionUtil.errorIf(isDeleted && !isAdmin,
                              ErrorCodeEnum.COMMENT_NOT_EXISTS, "评论已删除，无法操作");
    }

    /**
     * 验证评论作者权限
     *
     * @param authorId 评论作者ID
     */
    private void validateCommentAuthorPermission(Long authorId) {
        boolean isAdmin = ReqInfoContext.getContext().isAdmin();
        boolean isAuthor = Objects.equals(authorId, ReqInfoContext.getContext().getUserId());
        ExceptionUtil.errorIf(!isAdmin && !isAuthor,
                              ErrorCodeEnum.FORBID_ERROR_MIXED,
                              "非管理员无权限操作他人评论");
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
        ExceptionUtil.errorIf(timeDiff.toHours() > 24,
                              ErrorCodeEnum.FORBIDDEN_OPERATION, "评论发布超过24小时，不允许编辑");
    }

    /**
     * 检查并删除评论
     *
     * @param comment 评论DO对象
     * @return 是否执行了更新操作
     */
    private boolean checkAndDeleteComment(CommentDO comment) {
        // 检查状态是否需要更新
        if (Objects.equals(comment.getDeleted(), YesOrNoEnum.YES.getCode())) {
            return false;
        }

        // 执行状态更新
        comment.setDeleted(YesOrNoEnum.YES.getCode());
        commentDAO.updateById(comment);
        return true;
    }

    private CommentDO getParentComment(Long parentCommentId) {
        if (NumUtil.nullOrZero(parentCommentId)) {
            return null;
        }

        CommentDO parent = commentDAO.getById(parentCommentId);

        ExceptionUtil.requireValid(parent, ErrorCodeEnum.COMMENT_NOT_EXISTS, "parentCommentI=" + parentCommentId);
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
                ArticleDO article = articleService.getArticleById(comment.getArticleId());
                if (article != null) {
                    commentMy.setArticleTitle(article.getTitle());
                }
            } catch (Exception e) {
                log.warn("获取文章信息失败, articleId={}", comment.getArticleId(), e);
                commentMy.setArticleTitle("文章已删除");
            }

            // 填充父评论内容
            if (!NumUtil.nullOrZero(comment.getParentCommentId())) {
                try {
                    CommentDO parentComment = commentDAO.getById(comment.getParentCommentId());
                    if (parentComment != null && !Objects.equals(parentComment.getDeleted(),
                                                                 YesOrNoEnum.YES.getCode())) {
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
     * 通用分页查询方法
     */
    private <T> PageVO<T> executePageQuery(Supplier<IPage<CommentDO>> querySupplier,
                                           Function<List<CommentDO>, List<T>> dataBuilder) {
        // 1. 执行分页查询
        IPage<CommentDO> comments = querySupplier.get();

        if (comments.getRecords().isEmpty()) {
            return PageHelper.empty();
        }

        // 2. 构建结果数据
        List<T> resultData = dataBuilder.apply(comments.getRecords());

        // 3. 构建分页结果
        IPage<T> resultPage = new Page<>(comments.getCurrent(), comments.getSize(), comments.getTotal());
        resultPage.setRecords(resultData);

        return PageHelper.build(resultPage);
    }
}