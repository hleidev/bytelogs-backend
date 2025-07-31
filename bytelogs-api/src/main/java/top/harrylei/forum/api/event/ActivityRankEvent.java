package top.harrylei.forum.api.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class UserActivityEvent {

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
    private Integer actionType;

    /**
     * 目标类型
     */
    private Integer targetType;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 事件时间戳
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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