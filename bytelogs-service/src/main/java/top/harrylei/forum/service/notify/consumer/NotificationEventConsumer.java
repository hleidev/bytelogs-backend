package top.harrylei.forum.service.notify.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import top.harrylei.forum.api.model.event.NotificationEvent;
import top.harrylei.forum.core.common.constans.KafkaTopics;
import top.harrylei.forum.service.notify.service.NotifyMsgService;

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

    /**
     * 处理通知事件
     *
     * @param event     通知事件
     * @param partition 分区
     * @param offset    偏移量
     */
    @KafkaListener(topics = KafkaTopics.NOTIFICATION_EVENTS, containerFactory = "kafkaListenerContainerFactory")
    public void handleNotificationEvent(NotificationEvent event,
                                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                        @Header(KafkaHeaders.OFFSET) long offset) {

        try {
            // 处理通知事件
            notifyMsgService.saveNotificationFromEvent(event);
        } catch (Exception e) {
            log.error("处理通知事件失败: eventId={}, event={}", event.getEventId(), event, e);
            throw e;
        }
    }

    /**
     * 处理系统事件
     *
     * @param event     系统事件
     * @param partition 分区
     * @param offset    偏移量
     */
    @KafkaListener(topics = KafkaTopics.SYSTEM_EVENTS, containerFactory = "kafkaListenerContainerFactory")
    public void handleSystemEvent(NotificationEvent event,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                  @Header(KafkaHeaders.OFFSET) long offset) {

        try {
            // 系统事件也通过通知服务处理
            notifyMsgService.saveNotificationFromEvent(event);


        } catch (Exception e) {
            log.error("处理系统事件失败: eventId={}, event={}", event.getEventId(), event, e);
            throw e;
        }
    }
}