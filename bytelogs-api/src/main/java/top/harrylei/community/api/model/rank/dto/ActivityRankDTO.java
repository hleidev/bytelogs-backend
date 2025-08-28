package top.harrylei.community.api.model.rank.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 活跃度排名DTO
 *
 * @author harry
 */
@Data
@Accessors(chain = true)
public class ActivityRankDTO {

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