package top.harrylei.forum.service.notify.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.harrylei.forum.core.common.constans.RedisKeyConstants;
import top.harrylei.forum.core.util.RedisUtil;

import java.time.Duration;

/**
 * Kafka 消息幂等性服务
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaIdempotencyService {

    private final RedisUtil redisUtil;

    /**
     * 幂等性检查TTL - 24小时
     */
    private static final Duration KAFKA_IDEMPOTENCY_TTL = Duration.ofHours(24);

    /**
     * 尝试获取消息处理权限
     *
     * @param eventId Kafka消息的事件ID
     * @return true: 获得处理权限，可以处理; false: 消息已处理过，跳过
     */
    public boolean tryAcquireProcessingPermission(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            log.warn("尝试获取Kafka消息处理权限失败: eventId为空");
            return false;
        }

        String key = RedisKeyConstants.getKafkaIdempotencyKey(eventId);
        Boolean acquired = redisUtil.setIfAbsent(key, "processing", KAFKA_IDEMPOTENCY_TTL);

        if (Boolean.TRUE.equals(acquired)) {
            log.debug("获取Kafka消息处理权限成功: eventId={}", eventId);
            return true;
        } else {
            log.debug("Kafka消息已处理过，跳过: eventId={}", eventId);
            return false;
        }
    }

    /**
     * 检查消息是否已被处理
     *
     * @param eventId Kafka消息的事件ID
     * @return true: 已处理过; false: 未处理过
     */
    public boolean isMessageProcessed(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            log.warn("检查Kafka消息处理状态失败: eventId为空");
            return false;
        }

        String key = RedisKeyConstants.getKafkaIdempotencyKey(eventId);
        Boolean exists = redisUtil.exists(key);

        log.debug("检查Kafka消息处理状态: eventId={}, 已处理={}", eventId, exists);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * 释放消息处理权限
     *
     * @param eventId Kafka消息的事件ID
     * @return true: 释放成功; false: 释放失败
     */
    public boolean releaseProcessingPermission(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            log.warn("释放Kafka消息处理权限失败: eventId为空");
            return false;
        }

        String key = RedisKeyConstants.getKafkaIdempotencyKey(eventId);
        Boolean released = redisUtil.delete(key);

        if (Boolean.TRUE.equals(released)) {
            log.debug("释放Kafka消息处理权限成功: eventId={}", eventId);
            return true;
        } else {
            log.warn("释放Kafka消息处理权限失败: eventId={}", eventId);
            return false;
        }
    }

    /**
     * 标记消息处理完成
     * 将状态从"processing"更新为"completed"
     *
     * @param eventId Kafka消息的事件ID
     * @return true: 标记成功; false: 标记失败
     */
    public boolean markMessageAsProcessed(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            log.warn("标记Kafka消息处理完成失败: eventId为空");
            return false;
        }

        String key = RedisKeyConstants.getKafkaIdempotencyKey(eventId);
        Boolean marked = redisUtil.set(key, "completed", KAFKA_IDEMPOTENCY_TTL);

        if (Boolean.TRUE.equals(marked)) {
            log.debug("标记Kafka消息处理完成: eventId={}", eventId);
            return true;
        } else {
            log.warn("标记Kafka消息处理完成失败: eventId={}", eventId);
            return false;
        }
    }

    /**
     * 清理消息处理记录
     *
     * @param eventId Kafka消息的事件ID
     * @return true: 清理成功; false: 清理失败
     */
    public boolean cleanupProcessingRecord(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            log.warn("清理Kafka消息处理记录失败: eventId为空");
            return false;
        }

        return releaseProcessingPermission(eventId);
    }
}