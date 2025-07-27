package top.harrylei.forum.service.rank.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.forum.api.enums.rank.ActivityActionEnum;
import top.harrylei.forum.api.enums.rank.ActivityTargetEnum;
import top.harrylei.forum.api.model.base.BaseDO;

import java.io.Serial;
import java.time.LocalDate;

/**
 * 用户活跃度明细表
 *
 * @author harry
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_activity_detail")
public class UserActivityDetailDO extends BaseDO {

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
     * 目标ID
     */
    private Long targetId;

    /**
     * 目标类型
     */
    private ActivityTargetEnum targetType;

    /**
     * 获得积分
     */
    private Integer score;

    /**
     * 业务唯一键,用于防重
     */
    private String bizKey;

    /**
     * 行为日期
     */
    private LocalDate actionDate;
}