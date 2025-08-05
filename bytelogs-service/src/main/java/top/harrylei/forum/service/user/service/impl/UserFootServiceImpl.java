package top.harrylei.forum.service.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.harrylei.forum.api.enums.comment.ContentTypeEnum;
import top.harrylei.forum.api.enums.notify.NotifyTypeEnum;
import top.harrylei.forum.api.enums.rank.ActivityActionEnum;
import top.harrylei.forum.api.enums.rank.ActivityTargetEnum;
import top.harrylei.forum.api.enums.user.OperateTypeEnum;
import top.harrylei.forum.api.model.user.dto.ArticleFootCountDTO;
import top.harrylei.forum.api.model.user.dto.UserFootDTO;
import top.harrylei.forum.core.util.KafkaEventPublisher;
import top.harrylei.forum.core.util.NumUtil;
import top.harrylei.forum.core.util.RedisUtil;
import top.harrylei.forum.service.comment.repository.entity.CommentDO;
import top.harrylei.forum.service.user.converted.UserFootStructMapper;
import top.harrylei.forum.service.user.repository.dao.UserFootDAO;
import top.harrylei.forum.service.user.repository.entity.UserFootDO;
import top.harrylei.forum.service.user.service.UserFootService;

import java.time.Duration;
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
    private final RedisUtil redisUtil;
    private final KafkaEventPublisher kafkaEventPublisher;


    /**
     * 防重复提交锁过期时间（秒）
     */
    private static final Duration DUPLICATE_PREVENT_TIME = Duration.ofSeconds(2);


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
     * 记录阅读
     *
     * @param userId          用户ID
     * @param articleAuthorId 文章作者ID
     * @param articleId       文章ID
     * @return 是否成功操作
     */
    @Override
    public Boolean recordRead(Long userId, Long articleAuthorId, Long articleId) {
        return userFootAction(userId, OperateTypeEnum.READ, articleAuthorId, articleId, ContentTypeEnum.ARTICLE);
    }

    /**
     * 用户足迹操作的通用方法（添加防重复提交控制）
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

        // 防重复提交检查（只对点赞、收藏操作进行防重复提交控制）
        if (isActionNeedDuplicateCheck(type)) {
            String duplicateKey = buildDuplicateKey(userId, type, contentId, contentType);
            if (!redisUtil.tryPreventDuplicate(duplicateKey, DUPLICATE_PREVENT_TIME)) {
                log.warn("检测到重复提交: userId={} operateTypeEnum={} contentId={} contentType={}",
                         userId, type, contentId, contentType.getLabel());
                return false;
            }
        }

        UserFootDO userFoot = saveOrUpdateUserFoot(userId, type, contentAuthorId, contentId, contentType);

        if (userFoot == null) {
            log.warn("保存或更新{}用户足迹失败: userId={} operateTypeEnum={} contentAuthorId={} contentId={}",
                     contentType.getLabel(), userId, type, contentAuthorId, contentId);
            return false;
        }

        // 发布通知事件
        publishNotificationEvent(userId, type, contentAuthorId, contentId, contentType);

        // 发布活跃度事件
        publishActivityEvent(userId, type, contentId, contentType);

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
                return compareAndUpdate(userFoot::getReadState,
                                        userFoot::setReadState,
                                        operateTypeEnum.getStatusCode());
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

    /**
     * 判断操作类型是否需要防重复提交检查
     *
     * @param type 操作类型
     * @return 是否需要检查
     */
    private boolean isActionNeedDuplicateCheck(OperateTypeEnum type) {
        return type == OperateTypeEnum.PRAISE ||
                type == OperateTypeEnum.CANCEL_PRAISE ||
                type == OperateTypeEnum.COLLECTION ||
                type == OperateTypeEnum.CANCEL_COLLECTION;
    }

    /**
     * 构建防重复提交锁的key
     *
     * @param userId      用户ID
     * @param type        操作类型
     * @param contentId   内容ID
     * @param contentType 内容类型
     * @return 锁的key
     */
    private String buildDuplicateKey(Long userId, OperateTypeEnum type, Long contentId, ContentTypeEnum contentType) {
        return String.format("%d:%s:%d:%s", userId, type.name(), contentId, contentType.name());
    }

    /**
     * 发布通知事件
     *
     * @param userId          操作用户ID
     * @param type            操作类型
     * @param contentAuthorId 内容作者ID
     * @param contentId       内容ID
     * @param contentType     内容类型
     */
    private void publishNotificationEvent(Long userId,
                                          OperateTypeEnum type,
                                          Long contentAuthorId,
                                          Long contentId,
                                          ContentTypeEnum contentType) {
        try {
            NotifyTypeEnum notifyType = getNotifyTypeFromOperateType(type);
            if (notifyType == null) {
                // 不是需要通知的操作类型，跳过
                return;
            }

            // 发布通知事件
            kafkaEventPublisher.publishUserBehaviorEvent(userId, contentAuthorId, contentId, contentType, notifyType);

            log.debug("发布{}通知事件成功: userId={}, targetUserId={}, contentId={}",
                      notifyType.getLabel(),
                      userId,
                      contentAuthorId,
                      contentId);

        } catch (Exception e) {
            // 事件发布失败不影响主业务流程
            log.error("发布通知事件失败: userId={}, type={}, contentId={}", userId, type, contentId, e);
        }
    }

    /**
     * 将操作类型转换为通知类型
     *
     * @param operateType 操作类型
     * @return 通知类型，如果不需要通知则返回null
     */
    private NotifyTypeEnum getNotifyTypeFromOperateType(OperateTypeEnum operateType) {
        return switch (operateType) {
            case PRAISE -> NotifyTypeEnum.PRAISE;
            case COLLECTION -> NotifyTypeEnum.COLLECT;
            // 取消操作不发送通知
            case CANCEL_PRAISE, CANCEL_COLLECTION -> null;
            // 其他操作类型不在此处处理
            default -> null;
        };
    }

    /**
     * 发布活跃度事件
     *
     * @param userId      操作用户ID
     * @param type        操作类型
     * @param contentId   内容ID
     * @param contentType 内容类型
     */
    private void publishActivityEvent(Long userId,
                                      OperateTypeEnum type,
                                      Long contentId,
                                      ContentTypeEnum contentType) {
        try {
            ActivityActionEnum activityAction = getActivityActionFromOperateType(type);
            if (activityAction == null) {
                // 不是需要记录活跃度的操作类型，跳过
                return;
            }

            ActivityTargetEnum activityTarget = getActivityTargetFromContentType(contentType);
            if (activityTarget == null) {
                log.warn("未知的内容类型，无法发布活跃度事件: contentType={}", contentType);
                return;
            }

            // 发布活跃度事件
            kafkaEventPublisher.publishUserActivityEvent(userId, contentId, activityTarget, activityAction);

            log.debug("发布{}活跃度事件成功: userId={}, targetId={}, targetType={}",
                      activityAction.getLabel(), userId, contentId, activityTarget.getLabel());

        } catch (Exception e) {
            // 事件发布失败不影响主业务流程
            log.error("发布活跃度事件失败: userId={}, type={}, contentId={}", userId, type, contentId, e);
        }
    }

    /**
     * 将操作类型转换为活跃度行为类型
     *
     * @param operateType 操作类型
     * @return 活跃度行为类型，如果不需要记录活跃度则返回null
     */
    private ActivityActionEnum getActivityActionFromOperateType(OperateTypeEnum operateType) {
        return switch (operateType) {
            case PRAISE -> ActivityActionEnum.PRAISE;
            case COLLECTION -> ActivityActionEnum.COLLECT;
            case CANCEL_PRAISE -> ActivityActionEnum.CANCEL_PRAISE;
            case CANCEL_COLLECTION -> ActivityActionEnum.CANCEL_COLLECT;
            case READ -> ActivityActionEnum.READ;
            // 其他操作类型不在此处处理
            default -> null;
        };
    }

    /**
     * 将内容类型转换为活跃度目标类型
     *
     * @param contentType 内容类型
     * @return 活跃度目标类型
     */
    private ActivityTargetEnum getActivityTargetFromContentType(ContentTypeEnum contentType) {
        return switch (contentType) {
            case ARTICLE -> ActivityTargetEnum.ARTICLE;
            case COMMENT -> ActivityTargetEnum.COMMENT;
            default -> null;
        };
    }

    @Override
    public ArticleFootCountDTO getArticleFootCount(Long articleId) {
        ArticleFootCountDTO countDTO = new ArticleFootCountDTO();
        countDTO.setPraiseCount(userFootDAO.countPraiseByArticleId(articleId));
        countDTO.setCollectionCount(userFootDAO.countCollectionByArticleId(articleId));
        return countDTO;
    }
}