package top.harrylei.forum.api.model.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.forum.api.enums.ai.AIClientTypeEnum;
import top.harrylei.forum.api.enums.ai.AIMessageRoleEnum;
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
public class AIMessageVO extends BaseVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "对话ID")
    private Long conversationId;

    @Schema(description = "消息角色")
    private AIMessageRoleEnum role;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "使用的AI客户端类型")
    private AIClientTypeEnum vendor;

    @Schema(description = "使用的模型名称")
    private String model;

    @Schema(description = "输入Token消耗")
    private Integer inputTokens;

    @Schema(description = "AI生成Token消耗")
    private Integer outputTokens;
}