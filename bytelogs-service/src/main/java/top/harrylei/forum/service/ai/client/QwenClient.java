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
 * 通义千问客户端实现
 *
 * @author harry
 */
@Slf4j
@Component
public class QwenClient implements AIClient {

    private final AIConfig aiConfig;
    private final HttpClient httpClient;

    public QwenClient(AIConfig aiConfig, HttpClient aiHttpClient) {
        this.aiConfig = aiConfig;
        this.httpClient = aiHttpClient;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        AIConfig.ClientConfig config = aiConfig.getClientConfig(getType().getConfigKey());
        if (config == null || config.getApiKey() == null) {
            return ChatResponse.error("Qwen配置未找到");
        }

        try {
            // 构建请求体 - 通义千问API格式
            Map<String, Object> body = new HashMap<>();
            body.put("model", config.getModel());

            Map<String, Object> input = new HashMap<>();
            input.put("messages", convertMessages(request.getMessages()));
            body.put("input", input);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("max_tokens", config.getMaxTokens());
            parameters.put("temperature", config.getTemperature());
            body.put("parameters", parameters);

            String jsonBody = JsonUtil.toJson(body);

            if (log.isDebugEnabled()) {
                log.debug("Qwen请求: {}", jsonBody);
            }

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(config.getBaseUrl() + "/services/aigc/text-generation/generation"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .timeout(config.getTimeout())
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (log.isDebugEnabled()) {
                log.debug("Qwen响应状态: {}", response.statusCode());
                log.debug("Qwen API响应: {}", response.body());
            }

            if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                log.error("Qwen API错误: {} - {}", response.statusCode(), response.body());
                return ChatResponse.error("API调用失败: " + response.statusCode());
            }

            return parseResponse(response.body(), config.getModel());

        } catch (Exception e) {
            log.error("Qwen请求异常", e);
            return ChatResponse.error("请求异常: " + e.getMessage());
        }
    }

    @Override
    public AIClientTypeEnum getType() {
        return AIClientTypeEnum.QWEN;
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

    private ChatResponse parseResponse(String responseBody, String configModel) {
        JsonNode root = JsonUtil.parseToNode(responseBody);
        if (root == null) {
            return ChatResponse.error("响应解析失败");
        }

        try {
            JsonNode output = root.path("output");
            if (output.has("text")) {
                String content = output.path("text").asText();

                // 提取token使用量
                JsonNode usage = root.path("usage");
                Integer promptTokens = usage.has("input_tokens") ? usage.path("input_tokens").asInt() : null;
                Integer completionTokens = usage.has("output_tokens") ? usage.path("output_tokens").asInt() : null;
                Integer totalTokens = usage.has("total_tokens") ? usage.path("total_tokens").asInt() : null;

                ChatResponse response = ChatResponse.success(content, configModel);
                response.setInputTokens(promptTokens);
                response.setOutputTokens(completionTokens);
                response.setTotalTokens(totalTokens);
                return response;
            }
            return ChatResponse.error("无效的API响应");
        } catch (Exception e) {
            log.error("解析Qwen响应失败", e);
            return ChatResponse.error("响应解析失败");
        }
    }
}