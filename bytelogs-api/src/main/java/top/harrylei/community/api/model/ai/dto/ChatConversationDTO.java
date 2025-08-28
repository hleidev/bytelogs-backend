package top.harrylei.community.api.model.ai.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.community.api.enums.ai.ChatConversationStatusEnum;
import top.harrylei.community.api.model.base.BaseDTO;

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
public class ChatConversationDTO extends BaseDTO {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    
    private String title;
    
    private ChatConversationStatusEnum status;
    
    private Integer messageCount;
    
    private LocalDateTime lastMessageTime;
    
    private String lastMessagePreview;
}