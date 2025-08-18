package top.harrylei.forum.api.model.ai.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import top.harrylei.forum.api.enums.ai.AIClientTypeEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI对话请求
 *
 * @author harry
 */
@Data
@Schema(description = "AI对话请求")
public class ChatReq implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 4000, message = "消息内容不能超过4000个字符")
    @Schema(description = "用户消息内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    @Schema(description = "对话ID，新对话时不传")
    private Long conversationId;

    @Schema(description = "AI客户端类型")
    private AIClientTypeEnum model;
}