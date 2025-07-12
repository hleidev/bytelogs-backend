package top.harrylei.forum.service.notify.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import top.harrylei.forum.api.model.event.NotificationEvent;
import top.harrylei.forum.core.common.constans.KafkaTopics;
import top.harrylei.forum.service.notify.service.NotificationService;

/**
 * 通知事件消费者
 *
 * @author harry
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    /**
     * 处理通知事件
     *
     * @param event     通知事件
     * @param partition 分区
     * @param offset    偏移量
     * @param ack       手动确认
     */
    @KafkaListener(topics = KafkaTopics.NOTIFICATION_EVENTS)
    public void handleNotificationEvent(@Payload NotificationEvent event,
                                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                        @Header(KafkaHeaders.OFFSET) long offset,
                                        Acknowledgment ack) {
        log.info("接收到通知事件: eventId={}, type={}, operateUser={}, targetUser={}, partition={}, offset={}",
                 event.getEventId(), event.getNotifyType(), event.getOperateUserId(),
                 event.getTargetUserId(), partition, offset);

        try {
            // 处理通知事件
            notificationService.processNotificationEvent(event);

            // 手动确认消息
            if (ack != null) {
                ack.acknowledge();
            }

            log.info("通知事件处理完成: eventId={}", event.getEventId());

        } catch (Exception e) {
            log.error("处理通知事件失败: eventId={}, event={}", event.getEventId(), event, e);
            // 注意：这里不确认消息，让 Kafka 重试
            // TODO 可以考虑死信队列或者最大重试次数
            throw e;
        }
    }

    /**
     * 处理系统事件
     *
     * @param event     系统事件
     * @param partition 分区
     * @param offset    偏移量
     * @param ack       手动确认
     */
    @KafkaListener(topics = KafkaTopics.SYSTEM_EVENTS)
    public void handleSystemEvent(@Payload NotificationEvent event,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                  @Header(KafkaHeaders.OFFSET) long offset,
                                  Acknowledgment ack) {
        log.info("接收到系统事件: eventId={}, type={}, targetUser={}, partition={}, offset={}",
                 event.getEventId(), event.getNotifyType(), event.getTargetUserId(), partition, offset);

        try {
            // 系统事件也通过通知服务处理
            notificationService.processNotificationEvent(event);

            // 手动确认消息
            if (ack != null) {
                ack.acknowledge();
            }

            log.info("系统事件处理完成: eventId={}", event.getEventId());

        } catch (Exception e) {
            log.error("处理系统事件失败: eventId={}, event={}", event.getEventId(), event, e);
            throw e;
        }
    }
}