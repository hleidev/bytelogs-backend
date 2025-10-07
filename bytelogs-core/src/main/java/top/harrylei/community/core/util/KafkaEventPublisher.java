package top.harrylei.community.core.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import top.harrylei.community.api.enums.article.ArticleStatisticsEnum;
import top.harrylei.community.api.enums.article.ContentTypeEnum;
import top.harrylei.community.api.enums.notify.NotifyTypeEnum;
import top.harrylei.community.api.enums.rank.ActivityActionEnum;
import top.harrylei.community.api.enums.rank.ActivityTargetEnum;
import top.harrylei.community.api.event.ActivityRankEvent;
import top.harrylei.community.api.event.ArticleStatisticsEvent;
import top.harrylei.community.api.event.BaseEvent;
import top.harrylei.community.api.event.NotificationEvent;
import top.harrylei.community.core.common.constans.KafkaTopics;
import top.harrylei.community.core.context.ReqInfoContext;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka 事件发布器
 *
 * @author harry
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 通用Kafka事件发送
     */
    private void sendEventToKafka(String topic, BaseEvent event, String eventType) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }

        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, event.getEventId(), event);
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("{}事件发送失败: eventId={}, event={}", eventType, event.getEventId(), event, ex);
                }
            });
        } catch (Exception e) {
            log.error("发送{}事件异常: event={}", eventType, event, e);
        }
    }


    /**
     * 便捷方法：发布用户行为通知事件
     *
     * @param operateUserId 操作用户ID
     * @param targetUserId  目标用户ID（接收通知的用户）
     * @param relatedId     关联内容ID
     * @param contentType   内容类型
     * @param notifyType    通知类型
     */
    public void publishUserBehaviorEvent(Long operateUserId, Long targetUserId,
                                         Long relatedId, ContentTypeEnum contentType,
                                         NotifyTypeEnum notifyType) {
        publishUserBehaviorEvent(operateUserId, targetUserId, relatedId, contentType, notifyType, null);
    }

    /**
     * 便捷方法：发布用户行为通知事件（带扩展信息）
     *
     * @param operateUserId 操作用户ID
     * @param targetUserId  目标用户ID（接收通知的用户）
     * @param relatedId     关联内容ID
     * @param contentType   内容类型
     * @param notifyType    通知类型
     * @param extra         扩展信息
     */
    public void publishUserBehaviorEvent(Long operateUserId, Long targetUserId,
                                         Long relatedId, ContentTypeEnum contentType,
                                         NotifyTypeEnum notifyType, String extra) {
        NotificationEvent event = NotificationEvent.builder()
                .operateUserId(operateUserId)
                .targetUserId(targetUserId)
                .relatedId(relatedId)
                .notifyType(notifyType)
                .contentType(contentType)
                .extra(extra)
                .source("user-behavior")
                .build();

        sendEventToKafka(KafkaTopics.NOTIFICATION_EVENTS, event, "通知");
    }

    /**
     * 发布用户活跃度事件
     *
     * @param userId     用户ID
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @param actionType 行为类型
     */
    public void publishUserActivityEvent(Long userId,
                                         Long targetId,
                                         ActivityTargetEnum targetType,
                                         ActivityActionEnum actionType) {
        publishUserActivityEvent(userId, targetId, targetType, actionType, null);
    }

    /**
     * 发布用户活跃度事件（带扩展信息）
     *
     * @param userId     用户ID
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @param actionType 行为类型
     * @param extra      扩展信息
     */
    public void publishUserActivityEvent(Long userId,
                                         Long targetId,
                                         ActivityTargetEnum targetType,
                                         ActivityActionEnum actionType,
                                         String extra) {
        ActivityRankEvent event = ActivityRankEvent.builder()
                .userId(userId)
                .actionType(actionType)
                .targetType(targetType)
                .targetId(targetId)
                .extra(extra)
                .source("activity-rank")
                .build();

        sendEventToKafka(KafkaTopics.ACTIVITY_RANK_EVENTS, event, "活跃度");
    }


    /**
     * 发布文章统计事件
     *
     * @param articleId  文章ID
     * @param actionType 统计操作类型
     */
    public void publishArticleStatisticsEvent(Long articleId, ArticleStatisticsEnum actionType) {
        try {
            // 从上下文获取用户信息
            Long userId = null;
            String extra = null;

            ReqInfoContext.ReqInfo context = ReqInfoContext.getContext();

            if (context.isLoggedIn()) {
                userId = context.getUserId();
                if (actionType == ArticleStatisticsEnum.INCREMENT_READ) {
                    extra = "user:" + userId;
                }
            } else if (actionType == ArticleStatisticsEnum.INCREMENT_READ) {
                // 未登录用户的阅读统计，使用IP地址
                extra = "ip:" + context.getClientIp();
            }

            ArticleStatisticsEvent event = ArticleStatisticsEvent.builder()
                    .articleId(articleId)
                    .userId(userId)
                    .actionType(actionType)
                    .extra(extra)
                    .source("article-statistics")
                    .build();

            sendEventToKafka(KafkaTopics.ARTICLE_STATISTICS_EVENTS, event, "文章统计");

            log.debug("发布文章统计事件成功: articleId={}, actionType={}", articleId, actionType.getLabel());

        } catch (Exception e) {
            log.error("发布文章统计事件失败: articleId={}, actionType={}", articleId, actionType, e);
        }
    }

}