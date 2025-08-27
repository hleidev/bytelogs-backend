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

        OpenAiApi deepseekApi = new OpenAiApi(baseUrl, apiKey);
        OpenAiChatModel deepseekModel = new OpenAiChatModel(deepseekApi);

        /*DeepSeekApi deepSeekApi = new DeepSeekApi.Builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();

        DeepSeekChatModel deepSeekChatModel = DeepSeekChatModel.builder()
                .deepSeekApi(deepSeekApi)
                .defaultOptions(DeepSeekChatOptions.builder()
                        .model(DeepSeekApi.ChatModel.DEEPSEEK_CHAT)
                        .temperature(0.7)
                        .maxTokens(4000)
                        .build())
                .build();*/

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

        OpenAiApi qwenApi = new OpenAiApi(baseUrl, apiKey);
        OpenAiChatModel qwenModel = new OpenAiChatModel(qwenApi);

        return ChatClient.create(qwenModel);
    }

    /**
     * OpenAI
     */
    @Bean
    @Qualifier("openaiChatClient")
    public ChatClient openaiChatClient(
            @Value("${spring.ai.openai.api-key}") String apiKey,
            @Value("${spring.ai.openai.base-url}") String baseUrl) {

        OpenAiApi openaiApi = new OpenAiApi(baseUrl, apiKey);
        OpenAiChatModel openaiModel = new OpenAiChatModel(openaiApi);

        return ChatClient.create(openaiModel);
    }
}