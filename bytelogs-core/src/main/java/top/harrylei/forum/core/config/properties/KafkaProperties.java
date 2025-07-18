package top.harrylei.forum.core.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Kafka配置属性
 *
 * @author harry
 */
@Data
@Component
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {

    /**
     * 并发消费者数
     */
    private Integer concurrency = 3;

    /**
     * Topic分区数
     */
    private Integer partitions = 3;

    /**
     * 副本数
     */
    private Integer replicas = 1;

    /**
     * 批次大小
     */
    private Integer batchSize = 16384;

    /**
     * 延迟时间(ms)
     */
    private Integer lingerMs = 50;

    /**
     * 最大拉取记录数
     */
    private Integer maxPollRecords = 100;

    /**
     * 会话超时时间(ms)
     */
    private Integer sessionTimeoutMs = 30000;

    /**
     * 心跳间隔(ms)
     */
    private Integer heartbeatIntervalMs = 10000;

    /**
     * 最小拉取字节数
     */
    private Integer fetchMinBytes = 1024;

    /**
     * 最大等待时间(ms)
     */
    private Integer fetchMaxWaitMs = 5000;

    /**
     * 请求超时时间(ms)
     */
    private Integer requestTimeoutMs = 30000;

    /**
     * 交付超时时间(ms)
     */
    private Integer deliveryTimeoutMs = 120000;

    /**
     * 重试初始间隔(ms)
     */
    private Long retryInitialInterval = 1000L;

    /**
     * 重试最大间隔(ms)
     */
    private Long retryMaxInterval = 16000L;

    /**
     * 重试最大时间(ms)
     */
    private Long retryMaxElapsedTime = 60000L;
}