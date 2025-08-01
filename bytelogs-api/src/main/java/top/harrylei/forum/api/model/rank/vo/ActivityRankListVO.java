package top.harrylei.forum.api.model.rank.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import top.harrylei.forum.api.model.rank.dto.ActivityRankDTO;

import java.util.List;

/**
 * 活跃度排行榜列表响应VO
 * 包装排行榜数据和用户个人排名信息
 *
 * @author harry
 */
@Data
@Accessors(chain = true)
@Schema(description = "活跃度排行榜列表响应")
public class ActivityRankListVO {

    /**
     * 排行榜列表
     */
    @Schema(description = "排行榜列表", example = "[{...}]")
    private List<ActivityRankDTO> rankList;

    /**
     * 当前用户排名信息（仅登录时返回）
     */
    @Schema(description = "当前用户排名信息")
    private ActivityRankVO currentUserRank;

}