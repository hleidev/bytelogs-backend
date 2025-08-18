package top.harrylei.forum.api.model.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import top.harrylei.forum.api.enums.ai.AIClientTypeEnum;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI对话响应
 *
 * @author harry
 */
@Data
@Accessors(chain = true)
@Schema(description = "AI对话响应")
public class ChatRespVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "对话ID")
    private Long conversationId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "AI回复内容")
    private String reply;

    @Schema(description = "使用的AI客户端类型")
    private AIClientTypeEnum model;

    @Schema(description = "token消耗量")
    private Integer tokensUsed;

    @Schema(description = "回复时间")
    private LocalDateTime replyTime;
}