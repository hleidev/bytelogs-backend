package top.harrylei.community.service.ai.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.harrylei.community.api.enums.ai.ChatClientTypeEnum;
import top.harrylei.community.api.model.ai.req.ChatReq;
import top.harrylei.community.service.ai.config.AiProviderConfig;

/**
 * AI聊天选项适配器
 *
 * @author harry
 */
@Slf4j
@Component
public class ChatOptionsAdapter {

    private final AiProviderConfig aiProviderConfig;

    public ChatOptionsAdapter(AiProviderConfig aiProviderConfig) {
        this.aiProviderConfig = aiProviderConfig;
    }

    /**
     * 根据提供商类型构建相应的ChatOptions
     *
     * @param chatReq  聊天请求
     * @param provider AI提供商类型
     * @return 对应的ChatOptions对象，如果不需要特殊配置则返回null
     */
    public Object buildChatOptions(ChatReq chatReq, ChatClientTypeEnum provider) {
        if (provider == null) {
            provider = aiProviderConfig.getDefaultProvider();
        }

        return switch (provider) {
            case DEEPSEEK, QWEN, OPENAI -> buildOpenAiOptions(chatReq, provider);
            // TODO 未来可以扩展其他提供商的特定Options
            // case CLAUDE -> buildClaudeOptions(chatReq, provider);
        };
    }

    /**
     * 构建OpenAI的ChatOptions
     */
    private OpenAiChatOptions buildOpenAiOptions(ChatReq chatReq, ChatClientTypeEnum provider) {
        OpenAiChatOptions.Builder builder = OpenAiChatOptions.builder();
        boolean hasOptions = false;

        // 1. 设置模型
        String model = determineModel(chatReq.getModel(), provider);
        if (StringUtils.hasText(model)) {
            builder.withModel(model);
            hasOptions = true;
        }

        // 2. 设置温度参数
        if (chatReq.getTemperature() != null) {
            // 验证温度参数范围
            float temperature = validateTemperature(chatReq.getTemperature(), provider);
            builder.withTemperature((double) temperature);
            hasOptions = true;
        }

        // 3. 设置最大Token数
        if (chatReq.getMaxTokens() != null) {
            int maxTokens = validateMaxTokens(chatReq.getMaxTokens(), provider);
            builder.withMaxTokens(maxTokens);
            hasOptions = true;
        }

        log.debug("构建{}的ChatOptions: model={}, temperature={}, maxTokens={}",
                provider.getLabel(), model, chatReq.getTemperature(), chatReq.getMaxTokens());

        return hasOptions ? builder.build() : null;
    }

    /**
     * 确定使用的模型名称
     * 优先级: 用户指定 > 提供商默认模型
     */
    private String determineModel(String userModel, ChatClientTypeEnum provider) {
        if (StringUtils.hasText(userModel)) {
            return userModel;
        }

        // 从配置中获取默认模型
        String defaultModel = aiProviderConfig.getDefaultModel(provider);
        if (StringUtils.hasText(defaultModel)) {
            return defaultModel;
        }

        // 配置缺失时抛出异常，强制用户修复配置
        throw new IllegalStateException(
                String.format("提供商%s的默认模型配置缺失，且用户未指定模型。请检查配置文件或在请求中指定model参数",
                        provider.getLabel())
        );
    }

    /**
     * 验证温度参数
     */
    private float validateTemperature(Float temperature, ChatClientTypeEnum provider) {
        if (temperature == null) {
            return 0.7f;
        }

        // 从配置中获取温度范围
        float[] range = aiProviderConfig.getTemperatureRange(provider);
        float min = range[0];
        float max = range[1];

        float validTemperature = Math.max(min, Math.min(temperature, max));
        if (validTemperature != temperature) {
            log.warn("温度参数{}超出{}允许范围[{}, {}]，已调整为{}",
                    temperature, provider.getLabel(), min, max, validTemperature);
        }

        return validTemperature;
    }

    /**
     * 验证最大Token数
     */
    private int validateMaxTokens(Integer maxTokens, ChatClientTypeEnum provider) {
        if (maxTokens == null || maxTokens <= 0) {
            return 4000;
        }

        // 从配置中获取最大Token限制
        int maxLimit = aiProviderConfig.getMaxTokens(provider);

        int validMaxTokens = Math.min(maxTokens, maxLimit);
        if (validMaxTokens != maxTokens) {
            log.warn("maxTokens参数{}超出{}允许上限{}，已调整为{}",
                    maxTokens, provider.getLabel(), maxLimit, validMaxTokens);
        }

        return validMaxTokens;
    }

    // TODO 未来扩展: Claude等其他提供商的Options构建
    /*
    private Object buildClaudeOptions(ChatReq chatReq, ChatClientTypeEnum provider) {
        // Claude特定的Options构建逻辑
        // return ClaudeOptions.builder()...
        return null;
    }
    */
}