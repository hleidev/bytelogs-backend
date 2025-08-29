package top.harrylei.community.api.model.article.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.harrylei.community.api.enums.article.CreamStatusEnum;

import java.util.List;

/**
 * 文章加精更新请求对象
 *
 * @author harry
 */
@Data
@Schema(description = "文章加精更新请求")
public class ArticleCreamUpdateReq {

    @NotEmpty(message = "文章ID列表不能为空")
    @Schema(description = "需要更新加精的文章ID列表", example = "[1, 2, 3]")
    private List<Long> articleIds;

    @NotNull(message = "状态类型不能为空")
    @Schema(description = "1-加精，0-取消加精", example = "1")
    private CreamStatusEnum creamStat;
}