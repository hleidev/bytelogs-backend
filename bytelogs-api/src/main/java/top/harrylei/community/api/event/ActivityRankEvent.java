package top.harrylei.community.api.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import top.harrylei.community.api.enums.rank.ActivityActionEnum;
import top.harrylei.community.api.enums.rank.ActivityTargetEnum;

import java.io.Serial;

/**
 * 用户活跃度事件
 *
 * @author harry
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ActivityRankEvent extends BaseEvent {

    @Serial
    private static final long serialVersionUID = 1L;

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
}