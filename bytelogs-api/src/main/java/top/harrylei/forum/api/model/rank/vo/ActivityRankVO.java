package top.harrylei.forum.api.model.rank.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 活跃度排名信息
 *
 * @author harry
 */
@Data
@Accessors(chain = true)
public class ActivityRankVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 排名
     */
    private Integer rank;

    /**
     * 积分
     */
    private Integer score;
}