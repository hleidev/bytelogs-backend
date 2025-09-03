package top.harrylei.community.service.ai.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import top.harrylei.community.api.enums.ai.ChatClientTypeEnum;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI提供商配置类 - 统一管理各个AI提供商的配置信息
 *
 * @author harry
 */
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProviderConfig {

    /**
     * 各个提供商的配置
     */
    private Map<String, ProviderInfo> providers = new HashMap<>();

    /**
     * 默认提供商
     */
    private String defaultProvider;

    /**
     * 获取默认提供商枚举
     */
    public ChatClientTypeEnum getDefaultProvider() {
        if (defaultProvider == null || defaultProvider.isBlank()) {
            throw new IllegalStateException("默认提供商未配置，请在配置文件中设置 ai.default-provider");
        }

        try {
            return ChatClientTypeEnum.valueOf(defaultProvider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                String.format("默认提供商配置'%s'无效，支持的提供商: %s", 
                    defaultProvider, 
                    Arrays.toString(ChatClientTypeEnum.values()))
            );
        }
    }

    /**
     * 获取提供商的默认模型
     */
    public String getDefaultModel(ChatClientTypeEnum provider) {
        if (provider == null) {
            return null;
        }

        String providerKey = provider.getName();
        ProviderInfo providerInfo = providers.get(providerKey);

        if (providerInfo != null && providerInfo.getDefaultModel() != null) {
            return providerInfo.getDefaultModel();
        }

        // 配置缺失时记录警告并返回null
        log.warn("提供商{}的默认模型配置缺失，请检查配置文件", provider.getLabel());
        return null;
    }


    /**
     * 获取提供商支持的模型列表
     */
    public List<String> getSupportedModels(ChatClientTypeEnum provider) {
        if (provider == null) {
            return List.of();
        }

        String providerKey = provider.getName();
        ProviderInfo providerInfo = providers.get(providerKey);

        if (providerInfo != null && providerInfo.getSupportedModels() != null) {
            return providerInfo.getSupportedModels();
        }

        // 配置缺失时返回空列表并记录警告
        log.warn("提供商{}的支持模型列表配置缺失，请检查配置文件", provider.getLabel());
        return List.of();
    }

    /**
     * 检查提供商是否启用
     */
    public boolean isProviderEnabled(ChatClientTypeEnum provider) {
        if (provider == null) {
            return false;
        }

        String providerKey = provider.getName();
        ProviderInfo providerInfo = providers.get(providerKey);

        return providerInfo != null && providerInfo.isEnabled();
    }

    /**
     * 获取提供商的温度参数范围
     */
    public float[] getTemperatureRange(ChatClientTypeEnum provider) {
        if (provider == null) {
            return new float[]{0.0f, 2.0f};
        }

        String providerKey = provider.getName();
        ProviderInfo providerInfo = providers.get(providerKey);

        if (providerInfo != null && providerInfo.getTemperatureRange() != null &&
                providerInfo.getTemperatureRange().length == 2) {
            return providerInfo.getTemperatureRange();
        }

        // 默认范围
        return new float[]{0.0f, 2.0f};
    }

    /**
     * 获取提供商的最大Token限制
     */
    public int getMaxTokens(ChatClientTypeEnum provider) {
        if (provider == null) {
            return 4000;
        }

        String providerKey = provider.getName();
        ProviderInfo providerInfo = providers.get(providerKey);

        if (providerInfo != null && providerInfo.getMaxTokens() != null) {
            return providerInfo.getMaxTokens();
        }

        // 配置缺失时使用通用默认值并记录警告
        log.warn("提供商{}的最大Token配置缺失，使用默认值4000", provider.getLabel());
        return 4000;
    }

    /**
     * 单个提供商的配置信息
     */
    @Data
    public static class ProviderInfo {
        /**
         * 是否启用
         */
        private boolean enabled = true;

        /**
         * 默认模型
         */
        private String defaultModel;

        /**
         * 支持的模型列表（可选，用于前端展示）
         */
        private List<String> supportedModels;

        /**
         * 温度参数范围 [min, max]
         */
        private float[] temperatureRange;

        /**
         * 最大Token数限制
         */
        private Integer maxTokens;

        /**
         * 其他扩展配置
         */
        private Map<String, Object> extra = new HashMap<>();
    }
}