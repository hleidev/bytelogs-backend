package top.harrylei.forum.service.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.harrylei.forum.api.model.enums.OperateTypeEnum;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.enums.comment.ContentTypeEnum;
import top.harrylei.forum.core.util.NumUtil;
import top.harrylei.forum.service.comment.repository.entity.CommentDO;
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
        saveOrUpdateUserFoot(ContentTypeEnum.ARTICLE,
                             comment.getArticleId(),
                             articleAuthorId,
                             comment.getUserId(),
                             OperateTypeEnum.COMMENT);

        // 如果是回复评论，保存对父评论的回复足迹
        if (!NumUtil.nullOrZero(comment.getParentCommentId()) && !NumUtil.nullOrZero(parentCommentAuthorId)) {
            saveOrUpdateUserFoot(ContentTypeEnum.COMMENT,
                                 comment.getParentCommentId(),
                                 parentCommentAuthorId,
                                 comment.getUserId(),
                                 OperateTypeEnum.COMMENT);
        }
    }

    /**
     * 保存或更新状态信息
     *
     * @param contentTypeEnum 内容类型：博文 + 评论
     * @param contentId       内容id
     * @param authorId        作者
     * @param userId          操作人
     * @param operateTypeEnum 操作类型：点赞，评论，收藏等
     */
    public UserFootDO saveOrUpdateUserFoot(ContentTypeEnum contentTypeEnum,
                                           Long contentId,
                                           Long authorId,
                                           Long userId,
                                           OperateTypeEnum operateTypeEnum) {
        UserFootDO userFoot = userFootDAO.getByContentAndUserId(contentId, contentTypeEnum.getCode(), userId);
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
                                        operateTypeEnum.getCode());
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