package top.harrylei.forum.api.model.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.forum.api.enums.ai.AIConversationStatusEnum;
import top.harrylei.forum.api.model.base.BaseVO;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 对话详情VO
 *
 * @author harry
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "对话详情")
public class AIConversationDetailVO extends BaseVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "对话标题")
    private String title;

    @Schema(description = "对话状态")
    private AIConversationStatusEnum status;

    @Schema(description = "消息列表")
    private List<AIMessageVO> messages;
}