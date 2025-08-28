package top.harrylei.community.service.notify.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import top.harrylei.community.api.event.NotificationEvent;
import top.harrylei.community.core.common.constans.KafkaTopics;
import top.harrylei.community.api.exception.NonRetryableException;
import top.harrylei.community.api.exception.RetryableException;
import top.harrylei.community.service.notify.service.KafkaIdempotencyService;
import top.harrylei.community.service.notify.service.NotifyMsgService;

/**
 * 通知事件消费者
 *
 * @author harry
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final NotifyMsgService notifyMsgService;
    private final KafkaIdempotencyService kafkaIdempotencyService;

    /**
     * 处理通知事件
     *
     * @param event          通知事件
     * @param partition      分区
     * @param offset         偏移量
     * @param acknowledgment 手动确认
     */
    @KafkaListener(topics = KafkaTopics.NOTIFICATION_EVENTS, containerFactory = "notificationKafkaListenerContainerFactory")
    public void handleNotificationEvent(@Payload NotificationEvent event,
                                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                        @Header(KafkaHeaders.OFFSET) long offset,
                                        Acknowledgment acknowledgment) {

        log.debug("收到通知事件: eventId={}, partition={}, offset={}", event.getEventId(), partition, offset);

        try {
            // 参数验证
            if (event.getEventId() == null || event.getEventId().trim().isEmpty()) {
                log.error("通知事件eventId为空: event={}", event);
                acknowledgment.acknowledge();
                throw new NonRetryableException("通知事件eventId为空");
            }

            // 幂等性检查 - 尝试获取消息处理权限
            if (!kafkaIdempotencyService.tryAcquireProcessingPermission(event.getEventId())) {
                log.debug("通知事件已处理过，跳过: eventId={}", event.getEventId());
                acknowledgment.acknowledge();
                return;
            }

            // 处理通知事件
            notifyMsgService.saveNotificationFromEvent(event);

            // 标记消息处理完成
            kafkaIdempotencyService.markMessageAsProcessed(event.getEventId());

            // 手动确认消息
            acknowledgment.acknowledge();
            log.debug("通知事件处理成功: eventId={}", event.getEventId());

        } catch (IllegalArgumentException e) {
            // 参数错误，不重试，释放处理权限
            log.error("通知事件参数错误: eventId={}, error={}", event.getEventId(), e.getMessage());
            kafkaIdempotencyService.releaseProcessingPermission(event.getEventId());
            // 确认消息，避免重复处理
            acknowledgment.acknowledge();
            throw new NonRetryableException("通知事件参数错误", e);

        } catch (Exception e) {
            log.error("处理通知事件失败: eventId={}, event={}", event.getEventId(), event, e);
            // 释放处理权限，允许重试
            kafkaIdempotencyService.releaseProcessingPermission(event.getEventId());
            // 不确认消息，让重试机制处理
            throw new RetryableException("处理通知事件失败", e);
        }
    }

    /**
     * 处理系统事件
     *
     * @param event          系统事件
     * @param partition      分区
     * @param offset         偏移量
     * @param acknowledgment 手动确认
     */
    @KafkaListener(topics = KafkaTopics.SYSTEM_EVENTS, containerFactory = "notificationKafkaListenerContainerFactory")
    public void handleSystemEvent(@Payload NotificationEvent event,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                  @Header(KafkaHeaders.OFFSET) long offset,
                                  Acknowledgment acknowledgment) {

        log.debug("收到系统事件: eventId={}, partition={}, offset={}", event.getEventId(), partition, offset);

        try {
            // 参数验证
            if (event.getEventId() == null || event.getEventId().trim().isEmpty()) {
                log.error("系统事件eventId为空: event={}", event);
                acknowledgment.acknowledge();
                throw new NonRetryableException("系统事件eventId为空");
            }

            // 幂等性检查 - 尝试获取消息处理权限
            if (!kafkaIdempotencyService.tryAcquireProcessingPermission(event.getEventId())) {
                log.debug("系统事件已处理过，跳过: eventId={}", event.getEventId());
                acknowledgment.acknowledge();
                return;
            }

            // 系统事件也通过通知服务处理
            notifyMsgService.saveNotificationFromEvent(event);

            // 标记消息处理完成
            kafkaIdempotencyService.markMessageAsProcessed(event.getEventId());

            // 手动确认消息
            acknowledgment.acknowledge();
            log.debug("系统事件处理成功: eventId={}", event.getEventId());

        } catch (IllegalArgumentException e) {
            // 参数错误，不重试，释放处理权限
            log.error("系统事件参数错误: eventId={}, error={}", event.getEventId(), e.getMessage());
            kafkaIdempotencyService.releaseProcessingPermission(event.getEventId());
            // 确认消息，避免重复处理
            acknowledgment.acknowledge();
            throw new NonRetryableException("系统事件参数错误", e);

        } catch (Exception e) {
            log.error("处理系统事件失败: eventId={}, event={}", event.getEventId(), event, e);
            // 释放处理权限，允许重试
            kafkaIdempotencyService.releaseProcessingPermission(event.getEventId());
            // 不确认消息，让重试机制处理
            throw new RetryableException("处理系统事件失败", e);
        }
    }
}