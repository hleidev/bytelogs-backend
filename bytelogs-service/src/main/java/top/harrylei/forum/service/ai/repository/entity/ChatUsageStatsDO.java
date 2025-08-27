package top.harrylei.forum.service.ai.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.forum.api.model.base.BaseDO;
import top.harrylei.forum.api.enums.ai.ChatClientTypeEnum;

import java.io.Serial;
import java.time.LocalDate;

/**
 * 聊天使用统计表实体
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName("chat_usage_stats")
public class ChatUsageStatsDO extends BaseDO {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 统计日期
     */
    @TableField("stat_date")
    private LocalDate date;

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
     * 消息数量
     */
    @TableField("message_count")
    private Integer messageCount;

    /**
     * 会话数量
     */
    @TableField("conversation_count")
    private Integer conversationCount;

    /**
     * 提示词Token总数
     */
    @TableField("prompt_tokens")
    private Long promptTokens;

    /**
     * 完成Token总数
     */
    @TableField("completion_tokens")
    private Long completionTokens;

    /**
     * 总Token数
     */
    @TableField("total_tokens")
    private Long totalTokens;

}