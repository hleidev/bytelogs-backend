package top.harrylei.forum.api.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.harrylei.forum.api.enums.rank.ActivityActionEnum;
import top.harrylei.forum.api.enums.rank.ActivityTargetEnum;

import java.time.LocalDateTime;

/**
 * 用户活跃度事件
 *
 * @author harry
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityEvent {

    /**
     * 事件ID
     */
    private String eventId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 行为类型
     */
    private ActivityActionEnum actionType;

    /**
     * 目标类型
     */
    private ActivityTargetEnum targetType;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 事件时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 事件来源
     */
    private String source;

    /**
     * 扩展信息
     */
    private String extra;
}