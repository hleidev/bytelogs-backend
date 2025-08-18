package top.harrylei.forum.service.ai.client;

import lombok.Data;

/**
 * 统一聊天响应
 *
 * @author harry
 */
@Data
public class ChatResponse {

    /**
     * AI响应内容
     */
    private String content;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 是否成功
     */
    private boolean success = true;

    /**
     * 输入Token消耗（用户消息和上下文）
     */
    private Integer inputTokens;

    /**
     * AI生成Token消耗
     */
    private Integer outputTokens;

    /**
     * 总Token消耗
     */
    private Integer totalTokens;

    /**
     * 错误信息（失败时）
     */
    private String error;

    public static ChatResponse success(String content, String model) {
        ChatResponse response = new ChatResponse();
        response.setContent(content);
        response.setModel(model);
        response.setSuccess(true);
        return response;
    }

    public static ChatResponse error(String error) {
        ChatResponse response = new ChatResponse();
        response.setSuccess(false);
        response.setError(error);
        return response;
    }
}