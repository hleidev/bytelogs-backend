package top.harrylei.forum.api.model.rank.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.forum.api.enums.rank.ActivityRankTypeEnum;
import top.harrylei.forum.api.model.base.BasePage;

/**
 * 活跃度排行榜查询参数
 *
 * @author harry
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "活跃度排行榜查询参数")
public class ActivityRankQueryParam extends BasePage {

    /**
     * 排行榜类型
     */
    @Schema(description = "排行榜类型", example = "1")
    @NotNull(message = "排行榜类型不能为空")
    private ActivityRankTypeEnum type;
}