package top.harrylei.community.service.ai.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.api.enums.ai.ChatMessageRoleEnum;
import top.harrylei.community.api.enums.ai.ChatClientTypeEnum;
import top.harrylei.community.api.model.base.BaseDO;

import java.io.Serial;

/**
 * 聊天消息表实体
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("chat_message")
public class ChatMessageDO extends BaseDO {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 对话ID
     */
    @TableField("conversation_id")
    private Long conversationId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 消息类型
     */
    @TableField("message_type")
    private ChatMessageRoleEnum messageType;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * AI提供商
     */
    @TableField("provider")
    private ChatClientTypeEnum provider;

    /**
     * 模型名称
     */
    @TableField("model_name")
    private String modelName;

    /**
     * 提示词Token数
     */
    @TableField("prompt_tokens")
    private Long promptTokens = 0L;

    /**
     * 完成Token数
     */
    @TableField("completion_tokens")
    private Long completionTokens = 0L;

    /**
     * 总Token数
     */
    @TableField("total_tokens")
    private Long totalTokens = 0L;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableField("deleted")
    private DeleteStatusEnum deleted;
}