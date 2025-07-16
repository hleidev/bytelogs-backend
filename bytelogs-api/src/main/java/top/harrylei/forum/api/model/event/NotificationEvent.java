package top.harrylei.forum.api.model.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知事件模型
 *
 * @author harry
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 事件唯一ID
     */
    private String eventId;

    /**
     * 操作用户ID（发起行为的用户）
     */
    private Long operateUserId;

    /**
     * 被通知用户ID（接收通知的用户）
     */
    private Long targetUserId;

    /**
     * 关联内容ID（文章ID、评论ID等）
     */
    private Long relatedId;

    /**
     * 通知类型（点赞、评论、关注等）
     */
    private Integer notifyType;

    /**
     * 内容类型（文章、评论）
     */
    private Integer contentType;

    /**
     * 事件发生时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * 扩展信息（JSON格式，可存储额外的业务数据）
     */
    private String extra;

    /**
     * 事件来源（标识事件来源模块）
     */
    private String source;
}