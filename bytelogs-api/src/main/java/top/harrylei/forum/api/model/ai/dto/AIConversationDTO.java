package top.harrylei.forum.api.model.ai.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.forum.api.enums.ai.AIConversationStatusEnum;
import top.harrylei.forum.api.model.base.BaseDTO;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 对话信息DTO
 *
 * @author harry
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AIConversationDTO extends BaseDTO {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    
    private String title;
    
    private AIConversationStatusEnum status;
    
    private Integer messageCount;
    
    private LocalDateTime lastMessageTime;
    
    private String lastMessagePreview;
}