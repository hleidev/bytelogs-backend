package top.harrylei.forum.service.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.harrylei.forum.api.enums.ai.AIClientTypeEnum;
import top.harrylei.forum.core.config.AIConfig;
import top.harrylei.forum.core.util.JsonUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek客户端实现
 *
 * @author harry
 */
@Slf4j
@Component
public class DeepSeekClient implements AIClient {

    private final AIConfig aiConfig;
    private final HttpClient httpClient;

    public DeepSeekClient(AIConfig aiConfig, HttpClient aiHttpClient) {
        this.aiConfig = aiConfig;
        this.httpClient = aiHttpClient;
    }

    @Override
    public ChatResponse chat(ChatRequest request, String modelName) {
        AIConfig.ClientConfig config = aiConfig.getClientConfig(getType().getConfigKey());
        if (config == null || config.getApiKey() == null) {
            return ChatResponse.error("DeepSeek配置未找到");
        }

        // 获取具体模型配置
        AIConfig.ModelConfig modelConfig = aiConfig.getModelConfig(getType().getConfigKey(), modelName);
        if (modelConfig == null) {
            return ChatResponse.error("DeepSeek模型配置未找到: " + modelName);
        }

        try {
            // 构建请求体
            Map<String, Object> body = new HashMap<>();
            body.put("model", modelName);
            body.put("messages", convertMessages(request.getMessages()));
            body.put("max_tokens", modelConfig.getMaxTokens());
            body.put("temperature", modelConfig.getTemperature());

            String jsonBody = JsonUtil.toJson(body);

            if (log.isDebugEnabled()) {
                log.debug("DeepSeek请求: {}", jsonBody);
            }

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(config.getBaseUrl() + "/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .timeout(config.getTimeout())
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (log.isDebugEnabled()) {
                log.debug("DeepSeek响应状态: {}", response.statusCode());
            }

            if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                log.error("DeepSeek API错误: {} - {}", response.statusCode(), response.body());
                return ChatResponse.error("API调用失败: " + response.statusCode());
            }

            return parseResponse(response.body());

        } catch (Exception e) {
            log.error("DeepSeek请求异常", e);
            return ChatResponse.error("请求异常: " + e.getMessage());
        }
    }

    @Override
    public AIClientTypeEnum getType() {
        return AIClientTypeEnum.DEEPSEEK;
    }

    private List<Map<String, String>> convertMessages(List<ChatRequest.Message> messages) {
        return messages.stream()
                .map(msg -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("role", msg.getRole().getRole());
                    m.put("content", msg.getContent());
                    return m;
                })
                .toList();
    }

    private ChatResponse parseResponse(String responseBody) {
        JsonNode root = JsonUtil.parseToNode(responseBody);
        if (root == null) {
            return ChatResponse.error("响应解析失败");
        }

        try {
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                String content = choices.get(0).path("message").path("content").asText();
                String model = root.path("model").asText();

                // 解析token使用量
                Integer intputTokens = null;
                Integer outputTokens = null;
                Integer totalTokens = null;

                JsonNode usage = root.path("usage");
                if (!usage.isMissingNode()) {
                    intputTokens = usage.path("prompt_tokens").asInt(0);
                    outputTokens = usage.path("completion_tokens").asInt(0);
                    totalTokens = usage.path("total_tokens").asInt(0);
                }

                ChatResponse response = ChatResponse.success(content, model);
                response.setInputTokens(intputTokens);
                response.setOutputTokens(outputTokens);
                response.setTotalTokens(totalTokens);
                return response;
            }
            return ChatResponse.error("无效的API响应");
        } catch (Exception e) {
            log.error("解析DeepSeek响应失败", e);
            return ChatResponse.error("响应解析失败");
        }
    }
}