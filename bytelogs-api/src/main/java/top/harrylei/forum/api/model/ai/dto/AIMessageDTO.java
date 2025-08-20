package top.harrylei.forum.api.model.ai.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.forum.api.enums.ai.AIClientTypeEnum;
import top.harrylei.forum.api.enums.ai.AIMessageRoleEnum;
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
public class AIMessageDTO extends BaseDTO {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long conversationId;

    private Long userId;

    private AIMessageRoleEnum role;

    private String content;

    private AIClientTypeEnum vendor;
    
    private String model;

    private Integer inputTokens;
    
    private Integer outputTokens;
    
    private Integer totalTokens;
}