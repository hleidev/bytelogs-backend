package top.harrylei.forum.api.model.rank.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户活跃度统计概览VO
 *
 * @author harry
 */
@Data
@Accessors(chain = true)
@Schema(description = "用户活跃度统计概览")
public class ActivityStatsVO {

    /**
     * 日榜排名
     */
    @Schema(description = "日榜排名，null表示无排名数据", example = "15")
    private Integer dailyRank;

    /**
     * 日榜积分
     */
    @Schema(description = "日榜积分", example = "25")
    private Integer dailyScore;

    /**
     * 月榜排名
     */
    @Schema(description = "月榜排名，null表示无排名数据", example = "128")
    private Integer monthlyRank;

    /**
     * 月榜积分
     */
    @Schema(description = "月榜积分", example = "680")
    private Integer monthlyScore;

    /**
     * 总榜排名
     */
    @Schema(description = "总榜排名，null表示无排名数据", example = "1205")
    private Integer totalRank;

    /**
     * 总榜积分
     */
    @Schema(description = "总榜积分", example = "3250")
    private Integer totalScore;
}