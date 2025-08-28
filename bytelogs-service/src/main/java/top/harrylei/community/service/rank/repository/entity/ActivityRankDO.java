package top.harrylei.community.service.rank.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.community.api.model.base.BaseDO;

/**
 * 活跃度排行榜实体
 *
 * @author harry
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("activity_rank")
public class ActivityRankDO extends BaseDO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 排行榜类型: 1-总榜,2-月榜,3-日榜
     */
    private Integer rankType;

    /**
     * 排行榜周期: 日榜2025-01-15, 月榜2025-01, 总榜total
     */
    private String rankPeriod;

    /**
     * 积分
     */
    private Integer score;

    /**
     * 排名位置
     */
    @TableField("`rank`")
    private Integer rank;

    /**
     * 是否删除,0:未删除,1:已删除
     */
    private Integer deleted;
}