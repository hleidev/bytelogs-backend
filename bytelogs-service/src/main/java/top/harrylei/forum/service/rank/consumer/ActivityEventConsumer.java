package top.harrylei.forum.service.rank.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import top.harrylei.forum.api.event.ActivityRankEvent;
import top.harrylei.forum.core.common.constans.KafkaTopics;
import top.harrylei.forum.core.exception.NonRetryableException;
import top.harrylei.forum.core.exception.RetryableException;
import top.harrylei.forum.service.notify.service.KafkaIdempotencyService;
import top.harrylei.forum.service.rank.service.ActivityService;

/**
 * 用户活跃度事件消费者
 *
 * @author harry
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ActivityEventConsumer {

    private final ActivityService activityService;
    private final KafkaIdempotencyService kafkaIdempotencyService;

    /**
     * 处理用户活跃度事件
     *
     * @param event          活跃度事件
     * @param partition      分区
     * @param offset         偏移量
     * @param acknowledgment 手动确认
     */
    @KafkaListener(topics = KafkaTopics.ACTIVITY_RANK_EVENTS, containerFactory = "activityRankKafkaListenerContainerFactory")
    public void handleActivityEvent(@Payload ActivityRankEvent event,
                                    @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                    @Header(KafkaHeaders.OFFSET) long offset,
                                    Acknowledgment acknowledgment) {

        log.debug("收到活跃度事件: eventId={}, partition={}, offset={}", event.getEventId(), partition, offset);

        try {
            // 参数验证
            if (event.getEventId() == null || event.getEventId().trim().isEmpty()) {
                log.error("活跃度事件eventId为空: event={}", event);
                acknowledgment.acknowledge();
                throw new NonRetryableException("活跃度事件eventId为空");
            }

            if (event.getUserId() == null || event.getActionType() == null) {
                log.error("活跃度事件缺少必要参数: userId={}, actionType={}", event.getUserId(), event.getActionType());
                acknowledgment.acknowledge();
                throw new NonRetryableException("活跃度事件缺少必要参数");
            }

            // 幂等性检查 - 尝试获取消息处理权限
            if (!kafkaIdempotencyService.tryAcquireProcessingPermission(event.getEventId())) {
                log.debug("活跃度事件已处理过，跳过: eventId={}", event.getEventId());
                acknowledgment.acknowledge();
                return;
            }

            // 处理活跃度事件
            activityService.handleActivityEvent(event);

            // 标记消息处理完成
            kafkaIdempotencyService.markMessageAsProcessed(event.getEventId());

            // 手动确认消息
            acknowledgment.acknowledge();
            log.debug("活跃度事件处理成功: eventId={}, userId={}, action={}",
                      event.getEventId(), event.getUserId(), event.getActionType());

        } catch (IllegalArgumentException e) {
            // 参数错误，不重试，释放处理权限
            log.error("活跃度事件参数错误: eventId={}, error={}", event.getEventId(), e.getMessage());
            kafkaIdempotencyService.releaseProcessingPermission(event.getEventId());
            // 确认消息，避免重复处理
            acknowledgment.acknowledge();
            throw new NonRetryableException("活跃度事件参数错误", e);

        } catch (Exception e) {
            log.error("处理活跃度事件失败: eventId={}, event={}", event.getEventId(), event, e);
            // 释放处理权限，允许重试
            kafkaIdempotencyService.releaseProcessingPermission(event.getEventId());
            // 不确认消息，让重试机制处理
            throw new RetryableException("处理活跃度事件失败", e);
        }
    }
}