package top.harrylei.community.api.model.ai.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.community.api.enums.ai.ChatConversationStatusEnum;
import top.harrylei.community.api.model.base.BaseVO;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 对话信息VO
 *
 * @author harry
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "对话信息")
public class ChatConversationVO extends BaseVO {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "对话标题")
    private String title;

    @Schema(description = "对话状态")
    private ChatConversationStatusEnum status;

    @Schema(description = "消息数量")
    private Integer messageCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最后消息时间")
    private LocalDateTime lastMessageTime;

    @Schema(description = "最后消息内容预览")
    private String lastMessagePreview;
}