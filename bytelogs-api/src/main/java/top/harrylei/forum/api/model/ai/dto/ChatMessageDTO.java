package top.harrylei.forum.api.model.ai.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.forum.api.enums.ai.ChatClientTypeEnum;
import top.harrylei.forum.api.enums.ai.ChatMessageRoleEnum;
import top.harrylei.forum.api.model.base.BaseDTO;

import java.io.Serial;

/**
 * 消息信息DTO
 *
 * @author harry
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class ChatMessageDTO extends BaseDTO {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long conversationId;

    private Long userId;

    private ChatMessageRoleEnum role;

    private String content;

    private ChatClientTypeEnum vendor;
    
    private String model;

    private Long promptTokens;
    
    private Long completionTokens;
    
    private Long totalTokens;
}