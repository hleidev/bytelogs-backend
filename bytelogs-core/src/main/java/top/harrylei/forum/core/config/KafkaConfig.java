package top.harrylei.forum.core.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.ExponentialBackOff;
import top.harrylei.forum.api.event.ActivityRankEvent;
import top.harrylei.forum.api.event.NotificationEvent;
import top.harrylei.forum.core.common.constans.KafkaTopics;
import top.harrylei.forum.api.exception.NonRetryableException;

import java.util.Map;

/**
 * Kafka配置类
 *
 * @author harry
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.consumer.notification-group-id}")
    private String notificationGroupId;

    @Value("${spring.kafka.consumer.activity-group-id}")
    private String activityGroupId;

    @Value("${kafka.concurrency:3}")
    private Integer concurrency;

    @Value("${kafka.partitions:3}")
    private Integer partitions;

    @Value("${kafka.replicas:1}")
    private Integer replicas;

    @Value("${kafka.retry.initial-interval:1000}")
    private Long retryInitialInterval;

    @Value("${kafka.retry.max-interval:16000}")
    private Long retryMaxInterval;

    @Value("${kafka.retry.max-elapsed-time:60000}")
    private Long retryMaxElapsedTime;

    @Value("${kafka.retry.multiplier:2.0}")
    private Double retryMultiplier;

    /**
     * 通知事件消费者配置
     */
    @Bean
    public ConsumerFactory<String, NotificationEvent> notificationConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        // 覆盖消费者组ID
        props.put(ConsumerConfig.GROUP_ID_CONFIG, notificationGroupId);
        return new DefaultKafkaConsumerFactory<>(props, null, new JsonDeserializer<>(NotificationEvent.class));
    }

    /**
     * 用户活跃度事件消费者配置
     */
    @Bean
    public ConsumerFactory<String, ActivityRankEvent> activityRankConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        // 覆盖消费者组ID
        props.put(ConsumerConfig.GROUP_ID_CONFIG, activityGroupId);
        return new DefaultKafkaConsumerFactory<>(props, null, new JsonDeserializer<>(ActivityRankEvent.class));
    }

    /**
     * 错误处理器配置
     */
    @Bean
    public DefaultErrorHandler errorHandler() {
        // 指数退避重试策略
        ExponentialBackOff backOff = new ExponentialBackOff();
        backOff.setInitialInterval(retryInitialInterval);
        backOff.setMultiplier(retryMultiplier);
        backOff.setMaxInterval(retryMaxInterval);
        backOff.setMaxElapsedTime(retryMaxElapsedTime);

        DefaultErrorHandler handler = new DefaultErrorHandler(backOff);

        // 不重试的异常类型
        handler.addNotRetryableExceptions(IllegalArgumentException.class, NonRetryableException.class);

        return handler;
    }

    /**
     * 监听器容器工厂
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> notificationKafkaListenerContainerFactory(
            ConsumerFactory<String, NotificationEvent> notificationConsumerFactory,
            DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        // 基础配置
        factory.setConsumerFactory(notificationConsumerFactory);
        factory.setCommonErrorHandler(errorHandler);

        // 并发配置
        factory.setConcurrency(concurrency);

        // 手动立即确认模式
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        return factory;
    }

    /**
     * 用户活跃度事件监听器容器工厂
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ActivityRankEvent> activityRankKafkaListenerContainerFactory(
            ConsumerFactory<String, ActivityRankEvent> activityRankConsumerFactory,
            DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, ActivityRankEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(activityRankConsumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        factory.setConcurrency(concurrency);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        return factory;
    }

    /**
     * 用户活跃度事件Topic
     */
    @Bean
    public NewTopic activityRankTopic() {
        return TopicBuilder.name(KafkaTopics.ACTIVITY_RANK_EVENTS)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    /**
     * 通知事件Topic
     */
    @Bean
    public NewTopic notificationTopic() {
        return TopicBuilder.name(KafkaTopics.NOTIFICATION_EVENTS)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    /**
     * 系统事件Topic
     */
    @Bean
    public NewTopic systemTopic() {
        return TopicBuilder.name(KafkaTopics.SYSTEM_EVENTS)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }
}
