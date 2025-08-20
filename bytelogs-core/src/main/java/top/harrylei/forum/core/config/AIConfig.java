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
     * 默认使用的厂商类型
     */
    private String defaultClient;

    /**
     * 默认使用的模型
     */
    private String defaultModel;

    /**
     * 各个厂商的配置
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
        private Duration timeout;
        /**
         * 支持的模型列表
         */
        private Map<String, ModelConfig> models;
    }

    @Data
    public static class ModelConfig {
        private String displayName;
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

    /**
     * 获取指定厂商的模型配置
     */
    public ModelConfig getModelConfig(String clientType, String modelName) {
        ClientConfig clientConfig = getClientConfig(clientType);
        if (clientConfig == null || clientConfig.getModels() == null) {
            return null;
        }
        return clientConfig.getModels().get(modelName);
    }

    /**
     * 获取默认模型配置
     */
    public ModelConfig getDefaultModelConfig() {
        return getModelConfig(defaultClient, defaultModel);
    }

    /**
     * 验证厂商和模型的有效性
     */
    public boolean isValidVendorAndModel(String clientType, String modelName) {
        return getModelConfig(clientType, modelName) != null;
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