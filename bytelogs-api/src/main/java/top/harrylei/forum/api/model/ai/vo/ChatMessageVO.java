package top.harrylei.forum.api.model.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.forum.api.enums.ai.ChatClientTypeEnum;
import top.harrylei.forum.api.enums.ai.ChatMessageRoleEnum;
import top.harrylei.forum.api.model.base.BaseVO;

import java.io.Serial;

/**
 * 消息信息VO
 *
 * @author harry
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "消息信息")
public class ChatMessageVO extends BaseVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "对话ID")
    private Long conversationId;

    @Schema(description = "消息角色")
    private ChatMessageRoleEnum role;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "使用的AI客户端类型")
    private ChatClientTypeEnum vendor;

    @Schema(description = "使用的模型名称")
    private String model;

    @Schema(description = "输入Token消耗")
    private Integer promptTokens;

    @Schema(description = "AI生成Token消耗")
    private Integer completionTokens;
}