package top.harrylei.forum.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI使用限制配置
 *
 * @author harry
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai.limit")
public class AILimitConfig {

    /**
     * 每日最大消息数量
     */
    private Integer dailyMessageLimit = 50;

    /**
     * 每日最大Token消耗量
     */
    private Integer dailyTokenLimit = 100000;

    /**
     * 每日最大对话数量
     */
    private Integer dailyConversationLimit = 10;

    /**
     * 每小时最大消息数量（防刷）
     */
    private Integer hourlyMessageLimit = 20;

    /**
     * 单次消息最大长度
     */
    private Integer maxMessageLength = 4000;

    /**
     * 是否启用使用量限制
     */
    private Boolean enabled = true;
}