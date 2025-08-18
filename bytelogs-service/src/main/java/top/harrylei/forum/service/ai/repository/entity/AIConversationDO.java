package top.harrylei.forum.service.ai.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.forum.api.enums.ai.AIConversationStatusEnum;
import top.harrylei.forum.api.model.base.BaseDO;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * AI对话表实体
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("ai_conversation")
public class AIConversationDO extends BaseDO {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 对话标题
     */
    @TableField("title")
    private String title;

    /**
     * 对话状态
     */
    @TableField("status")
    private AIConversationStatusEnum status;

    /**
     * 消息数量
     */
    @TableField("message_count")
    private Integer messageCount;

    /**
     * 最后消息时间
     */
    @TableField("last_message_time")
    private LocalDateTime lastMessageTime;

    /**
     * 最后消息内容预览
     */
    @TableField("last_message_preview")
    private String lastMessagePreview;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableField("deleted")
    private Integer deleted;
}