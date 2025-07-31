package top.harrylei.forum.api.model.rank.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import top.harrylei.forum.api.enums.rank.ActivityRankTypeEnum;

/**
 * 用户活跃度排名信息
 *
 * @author harry
 */
@Data
@Accessors(chain = true)
@Schema(description = "用户活跃度排名信息")
public class UserActivityRankVO {

    /**
     * 排行榜类型
     */
    @Schema(description = "排行榜类型")
    private ActivityRankTypeEnum rankType;

    /**
     * 排名
     */
    @Schema(description = "排名，null表示无排名数据", example = "1205")
    private Long rank;

    /**
     * 积分
     */
    @Schema(description = "积分", example = "100")
    private Integer score;
}