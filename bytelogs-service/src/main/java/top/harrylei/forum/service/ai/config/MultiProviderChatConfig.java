package top.harrylei.forum.service.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 多厂商ChatClient配置
 *
 * @author harry
 */
@Slf4j
@Configuration
public class MultiProviderChatConfig {

    /**
     * DeepSeek
     */
    @Bean
    @Qualifier("deepseekChatClient")
    public ChatClient deepseekChatClient(
            @Value("${spring.ai.deepseek.api-key}") String apiKey,
            @Value("${spring.ai.deepseek.base-url}") String baseUrl) {

        log.info("初始化 DeepSeek ChatClient");
        OpenAiApi deepseekApi = new OpenAiApi(baseUrl, apiKey);
        OpenAiChatModel deepseekModel = new OpenAiChatModel(deepseekApi);

        return ChatClient.create(deepseekModel);
    }

    /**
     * 通义千问
     */
    @Bean
    @Qualifier("qwenChatClient")
    public ChatClient qwenChatClient(
            @Value("${spring.ai.qwen.api-key}") String apiKey,
            @Value("${spring.ai.qwen.base-url}") String baseUrl) {

        log.info("初始化 Qwen ChatClient");
        OpenAiApi qwenApi = new OpenAiApi(baseUrl, apiKey);
        OpenAiChatModel qwenModel = new OpenAiChatModel(qwenApi);

        return ChatClient.create(qwenModel);
    }
}