package top.harrylei.community.api.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import top.harrylei.community.api.enums.article.ContentTypeEnum;
import top.harrylei.community.api.enums.notify.NotifyTypeEnum;

import java.io.Serial;

/**
 * 通知事件模型
 *
 * @author harry
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NotificationEvent extends BaseEvent {

    @Serial
    private static final long serialVersionUID = 1L;

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
    private NotifyTypeEnum notifyType;

    /**
     * 内容类型（文章、评论）
     */
    private ContentTypeEnum contentType;
}