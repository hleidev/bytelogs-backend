package top.harrylei.forum.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

/**
 * AI模型统一配置
 *
 * @author harry
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AIConfig {

    /**
     * 默认使用的模型类型
     */
    private String defaultClient;

    /**
     * 各个模型的配置
     */
    private Map<String, ClientConfig> clients;

    /**
     * HTTP客户端配置
     */
    private HttpConfig http = new HttpConfig();

    @Data
    public static class ClientConfig {
        private String apiKey;
        private String baseUrl;
        private String model;
        private Duration timeout;
        private Integer maxTokens;
        private Double temperature;
    }

    /**
     * 获取指定客户端配置
     */
    public ClientConfig getClientConfig(String clientType) {
        return clients != null ? clients.get(clientType) : null;
    }

    /**
     * 获取默认客户端配置
     */
    public ClientConfig getDefaultClientConfig() {
        return getClientConfig(defaultClient);
    }

    @Data
    public static class HttpConfig {
        /**
         * 连接超时时间
         */
        private Duration connectTimeout = Duration.ofSeconds(30);
        
        /**
         * 读取超时时间
         */
        private Duration readTimeout = Duration.ofSeconds(60);
    }
}