package top.harrylei.forum.service.rank.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.forum.api.model.base.BaseDO;

import java.io.Serial;
import java.time.LocalDate;

/**
 * 用户活跃度积分表
 *
 * @author harry
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_activity_score")
public class UserActivityScoreDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 历史总积分
     */
    private Integer totalScore;

    /**
     * 今日积分
     */
    private Integer dailyScore;

    /**
     * 本周积分
     */
    private Integer weeklyScore;

    /**
     * 本月积分
     */
    private Integer monthlyScore;

    /**
     * 最后活跃日期
     */
    private LocalDate lastActiveDate;
}