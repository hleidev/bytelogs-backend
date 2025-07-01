package top.harrylei.forum.service.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.harrylei.forum.api.model.enums.OperateTypeEnum;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.enums.comment.ContentTypeEnum;
import top.harrylei.forum.api.model.vo.user.dto.UserFootDTO;
import top.harrylei.forum.core.util.NumUtil;
import top.harrylei.forum.service.comment.repository.entity.CommentDO;
import top.harrylei.forum.service.user.converted.UserFootStructMapper;
import top.harrylei.forum.service.user.repository.dao.UserFootDAO;
import top.harrylei.forum.service.user.repository.entity.UserFootDO;
import top.harrylei.forum.service.user.service.UserFootService;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 用户足迹服务实现
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserFootServiceImpl implements UserFootService {

    private final UserFootDAO userFootDAO;
    private final UserFootStructMapper userFootStructMapper;

    /**
     * 保存评论足迹
     *
     * @param comment               保存评论
     * @param articleAuthorId       文章作者
     * @param parentCommentAuthorId 父评论作者
     */
    @Override
    public void saveCommentFoot(CommentDO comment, Long articleAuthorId, Long parentCommentAuthorId) {
        // 保存对文章的评论足迹
        saveOrUpdateUserFoot(comment.getUserId(),
                             OperateTypeEnum.COMMENT,
                             articleAuthorId,
                             comment.getArticleId(),
                             ContentTypeEnum.ARTICLE);

        // 如果是回复评论，保存对父评论的回复足迹
        if (!NumUtil.nullOrZero(comment.getParentCommentId()) && !NumUtil.nullOrZero(parentCommentAuthorId)) {
            saveOrUpdateUserFoot(comment.getUserId(),
                                 OperateTypeEnum.COMMENT,
                                 parentCommentAuthorId,
                                 comment.getParentCommentId(),
                                 ContentTypeEnum.COMMENT);
        }
    }

    /**
     * 删除评论足迹
     *
     * @param comment               删除评论
     * @param articleAuthorId       文章作者
     * @param parentCommentAuthorId 父评论作者
     */
    @Override
    public void deleteCommentFoot(CommentDO comment, Long articleAuthorId, Long parentCommentAuthorId) {
        // 删除对文章的评论足迹
        saveOrUpdateUserFoot(comment.getUserId(),
                             OperateTypeEnum.DELETE_COMMENT,
                             articleAuthorId,
                             comment.getArticleId(),
                             ContentTypeEnum.ARTICLE);

        // 如果是回复评论，删除对父评论的回复足迹
        if (!NumUtil.nullOrZero(comment.getParentCommentId()) && !NumUtil.nullOrZero(parentCommentAuthorId)) {
            saveOrUpdateUserFoot(comment.getUserId(),
                                 OperateTypeEnum.DELETE_COMMENT,
                                 parentCommentAuthorId,
                                 comment.getParentCommentId(),
                                 ContentTypeEnum.COMMENT);
        }
    }

    /**
     * 查询用户足迹
     *
     * @param userId          用户ID
     * @param contentId       内容ID
     * @param contentTypeEnum 内容类型
     * @return 用户足迹传输对象
     */
    @Override
    public UserFootDTO getUserFoot(Long userId, Long contentId, ContentTypeEnum contentTypeEnum) {
        if (NumUtil.nullOrZero(userId) || NumUtil.nullOrZero(contentId) || contentTypeEnum == null) {
            return null;
        }
        UserFootDO userFoot = userFootDAO.getByContentAndUserId(userId, contentId, contentTypeEnum.getCode());
        return userFootStructMapper.toDTO(userFoot);
    }

    /**
     * 评论操作
     *
     * @param userId          用户ID
     * @param type            操作类型：点赞、收藏等
     * @param commentAuthorId 评论作者ID
     * @param commentId       评论ID
     * @return 是否成功操作
     */
    @Override
    public Boolean actionComment(Long userId, OperateTypeEnum type, Long commentAuthorId, Long commentId) {
        return userFootAction(userId, type, commentAuthorId, commentId, ContentTypeEnum.COMMENT);
    }

    /**
     * 文章操作
     *
     * @param userId          用户ID
     * @param type            操作类型：点赞、收藏等
     * @param articleAuthorId 文章作者ID
     * @param articleId       文章ID
     * @return 是否成功操作
     */
    @Override
    public Boolean actionArticle(Long userId, OperateTypeEnum type, Long articleAuthorId, Long articleId) {
        return userFootAction(userId, type, articleAuthorId, articleId, ContentTypeEnum.ARTICLE);
    }

    /**
     * 用户足迹操作的通用方法
     *
     * @param userId          用户ID
     * @param type            操作类型
     * @param contentAuthorId 内容作者ID
     * @param contentId       内容ID
     * @param contentType     内容类型
     * @return 是否成功操作
     */
    private Boolean userFootAction(Long userId,
                                   OperateTypeEnum type,
                                   Long contentAuthorId,
                                   Long contentId,
                                   ContentTypeEnum contentType) {
        if (NumUtil.nullOrZero(userId) || type == null || NumUtil.nullOrZero(contentId) ||
                NumUtil.nullOrZero(contentAuthorId)) {
            log.warn(
                    "执行{}操作参数无效: userId={} operateTypeEnum={} contentAuthorId={} contentId={}",
                    contentType.getLabel(),
                    userId,
                    type,
                    contentAuthorId,
                    contentId);
            return false;
        }

        UserFootDO userFoot = saveOrUpdateUserFoot(userId, type, contentAuthorId, contentId, contentType);
        if (userFoot == null) {
            log.warn("保存或更新{}用户足迹失败: userId={} operateTypeEnum={} contentAuthorId={} contentId={}",
                     contentType.getLabel(), userId, type, contentAuthorId, contentId);
            return false;
        }
        return true;
    }

    /**
     * 保存或更新状态信息
     *
     * @param userId          操作人
     * @param operateTypeEnum 操作类型：点赞，评论，收藏等
     * @param authorId        作者
     * @param contentId       内容id
     * @param contentTypeEnum 内容类型：博文 + 评论
     */
    public UserFootDO saveOrUpdateUserFoot(Long userId,
                                           OperateTypeEnum operateTypeEnum,
                                           Long authorId,
                                           Long contentId, ContentTypeEnum contentTypeEnum) {
        UserFootDO userFoot = userFootDAO.getByContentAndUserId(userId, contentId, contentTypeEnum.getCode());
        if (userFoot == null) {
            userFoot = new UserFootDO()
                    .setUserId(userId)
                    .setContentId(contentId)
                    .setContentUserId(authorId)
                    .setContentType(contentTypeEnum.getCode());
            setUserFootState(userFoot, operateTypeEnum);
            userFootDAO.save(userFoot);
        } else if (setUserFootState(userFoot, operateTypeEnum)) {
            userFootDAO.updateById(userFoot);
        }
        return userFoot;
    }

    private boolean setUserFootState(UserFootDO userFoot, OperateTypeEnum operateTypeEnum) {
        switch (operateTypeEnum) {
            case READ -> {
                userFoot.setReadState(YesOrNoEnum.YES.getCode());
                return true;
            }
            case COMMENT, DELETE_COMMENT -> {
                return compareAndUpdate(userFoot::getCommentState,
                                        userFoot::setCommentState,
                                        operateTypeEnum.getStatusCode());
            }
            case PRAISE, CANCEL_PRAISE -> {
                return compareAndUpdate(userFoot::getPraiseState,
                                        userFoot::setPraiseState,
                                        operateTypeEnum.getStatusCode());
            }
            case COLLECTION, CANCEL_COLLECTION -> {
                return compareAndUpdate(userFoot::getCollectionState,
                                        userFoot::setCollectionState,
                                        operateTypeEnum.getStatusCode());
            }
            default -> {
                return false;
            }
        }
    }

    private <T> boolean compareAndUpdate(Supplier<T> supplier, Consumer<T> consumer, T input) {
        if (Objects.equals(supplier.get(), input)) {
            return false;
        }
        consumer.accept(input);
        return true;
    }
}