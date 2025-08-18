package top.harrylei.forum.service.ai.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.forum.api.model.base.BaseDO;

import java.io.Serial;
import java.time.LocalDate;

/**
 * AI使用统计表实体
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName("ai_usage_stats")
public class AIUsageStatsDO extends BaseDO {
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
    @TableField("date")
    private LocalDate date;

    /**
     * 消息数量
     */
    @TableField("message_count")
    private Integer messageCount;

    /**
     * Token消耗总量
     */
    @TableField("tokens_used")
    private Integer tokensUsed;

    /**
     * 对话数量
     */
    @TableField("conversation_count")
    private Integer conversationCount;
}