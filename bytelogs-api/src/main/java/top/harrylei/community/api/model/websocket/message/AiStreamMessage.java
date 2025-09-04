package top.harrylei.community.api.model.websocket.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI流式响应消息数据
 *
 * @author harry
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI流式响应消息")
public class AiStreamMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "会话ID", example = "123")
    private Long conversationId;

    @Schema(description = "消息ID", example = "456")
    private Long messageId;

    @Schema(description = "流式内容片段", example = "这是AI的回答...")
    private String content;

    @Schema(description = "是否完成", example = "false")
    private Boolean finished;

    @Schema(description = "累计Token数", example = "150")
    private Integer totalTokens;

    @Schema(description = "错误信息")
    private String error;

    /**
     * 创建流式消息片段
     */
    public static AiStreamMessage chunk(Long conversationId, Long messageId, String content) {
        return AiStreamMessage.builder()
                .conversationId(conversationId)
                .messageId(messageId)
                .content(content)
                .finished(false)
                .build();
    }

    /**
     * 创建完成消息
     */
    public static AiStreamMessage finish(Long conversationId, Long messageId, Integer totalTokens) {
        return AiStreamMessage.builder()
                .conversationId(conversationId)
                .messageId(messageId)
                .finished(true)
                .totalTokens(totalTokens)
                .build();
    }

    /**
     * 创建错误消息
     */
    public static AiStreamMessage error(Long conversationId, String error) {
        return AiStreamMessage.builder()
                .conversationId(conversationId)
                .error(error)
                .finished(true)
                .build();
    }
}