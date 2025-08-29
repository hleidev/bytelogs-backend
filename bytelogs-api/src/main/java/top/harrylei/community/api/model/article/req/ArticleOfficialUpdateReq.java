package top.harrylei.community.api.model.article.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.harrylei.community.api.enums.article.OfficialStatusEnum;

import java.util.List;

/**
 * 文章官方更新请求对象
 *
 * @author harry
 */
@Data
@Schema(description = "文章官方更新请求")
public class ArticleOfficialUpdateReq {

    @NotEmpty(message = "文章ID列表不能为空")
    @Schema(description = "需要更新官方的文章ID列表", example = "[1, 2, 3]")
    private List<Long> articleIds;

    @NotNull(message = "状态类型不能为空")
    @Schema(description = "1-官方，0-取消官方", example = "1")
    private OfficialStatusEnum officialStat;
}