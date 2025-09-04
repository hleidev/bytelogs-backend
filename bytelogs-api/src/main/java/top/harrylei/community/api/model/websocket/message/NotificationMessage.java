package top.harrylei.community.api.model.websocket.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.harrylei.community.api.enums.notify.NotifyTypeEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通知消息数据
 *
 * @author harry
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通知消息")
public class NotificationMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "通知ID", example = "789")
    private Long notificationId;

    @Schema(description = "通知类型")
    private NotifyTypeEnum type;

    @Schema(description = "通知标题", example = "有人点赞了你的文章")
    private String title;

    @Schema(description = "通知内容", example = "用户张三点赞了你的文章《Spring Boot实战》")
    private String content;

    @Schema(description = "关联实体ID", example = "123")
    private Long entityId;

    @Schema(description = "关联实体类型", example = "ARTICLE")
    private String entityType;

    @Schema(description = "未读通知总数", example = "5")
    private Long unreadCount;

    @Schema(description = "跳转链接", example = "/article/123")
    private String linkUrl;

    /**
     * 从现有通知事件创建WebSocket消息
     */
    public static NotificationMessage fromNotifyEvent(Long notificationId, NotifyTypeEnum type,
                                                      String title, String content, Long entityId,
                                                      String entityType, Long unreadCount) {
        return NotificationMessage.builder()
                .notificationId(notificationId)
                .type(type)
                .title(title)
                .content(content)
                .entityId(entityId)
                .entityType(entityType)
                .unreadCount(unreadCount)
                .build();
    }
}