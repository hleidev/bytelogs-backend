package top.harrylei.forum.core.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import top.harrylei.forum.api.model.enums.NotifyTypeEnum;
import top.harrylei.forum.api.model.enums.comment.ContentTypeEnum;
import top.harrylei.forum.api.model.event.NotificationEvent;
import top.harrylei.forum.core.common.constans.KafkaTopics;

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

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    /**
     * 发布通知事件
     *
     * @param event 通知事件
     */
    public void publishNotificationEvent(NotificationEvent event) {
        // 设置事件基础信息
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }

        try {
            CompletableFuture<SendResult<String, NotificationEvent>> future =
                    kafkaTemplate.send(KafkaTopics.NOTIFICATION_EVENTS, event.getEventId(), event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("通知事件发送成功: eventId={}, type={}, operateUser={}, targetUser={}",
                             event.getEventId(), event.getNotifyType(),
                             event.getOperateUserId(), event.getTargetUserId());
                } else {
                    log.error("通知事件发送失败: eventId={}, event={}", event.getEventId(), event, ex);
                }
            });
        } catch (Exception e) {
            log.error("发送通知事件异常: event={}", event, e);
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

        publishNotificationEvent(event);
    }

    /**
     * 发布系统事件
     *
     * @param event 系统事件
     */
    public void publishSystemEvent(NotificationEvent event) {
        // 设置事件基础信息
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }
        if (event.getSource() == null) {
            event.setSource("system");
        }

        try {
            CompletableFuture<SendResult<String, NotificationEvent>> future =
                    kafkaTemplate.send(KafkaTopics.SYSTEM_EVENTS, event.getEventId(), event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("系统事件发送成功: eventId={}, type={}",
                             event.getEventId(), event.getNotifyType());
                } else {
                    log.error("系统事件发送失败: eventId={}, event={}", event.getEventId(), event, ex);
                }
            });
        } catch (Exception e) {
            log.error("发送系统事件异常: event={}", event, e);
        }
    }
}