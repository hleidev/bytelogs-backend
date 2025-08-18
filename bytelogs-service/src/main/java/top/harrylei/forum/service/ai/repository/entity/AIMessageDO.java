package top.harrylei.forum.service.ai.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.forum.api.enums.ai.AIClientTypeEnum;
import top.harrylei.forum.api.enums.ai.AIMessageRoleEnum;
import top.harrylei.forum.api.model.base.BaseDO;

import java.io.Serial;

/**
 * AI消息表实体
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("ai_message")
public class AIMessageDO extends BaseDO {
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
     * 消息角色
     */
    @TableField("role")
    private AIMessageRoleEnum role;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 使用的AI客户端类型
     */
    @TableField("model")
    private AIClientTypeEnum model;

    /**
     * 输入Token消耗（用户消息和上下文）
     */
    @TableField("input_tokens")
    private Integer inputTokens;

    /**
     * AI生成Token消耗
     */
    @TableField("output_tokens")
    private Integer outputTokens;

    /**
     * 总Token消耗
     */
    @TableField("total_tokens")
    private Integer totalTokens;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableField("deleted")
    private Integer deleted;
}