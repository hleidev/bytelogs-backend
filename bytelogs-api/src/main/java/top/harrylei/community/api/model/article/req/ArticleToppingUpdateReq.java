package top.harrylei.community.api.model.article.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.harrylei.community.api.enums.article.ToppingStatusEnum;

import java.util.List;

/**
 * 文章置顶更新请求对象
 *
 * @author harry
 */
@Data
@Schema(description = "文章置顶更新请求")
public class ArticleToppingUpdateReq {

    @NotEmpty(message = "文章ID列表不能为空")
    @Schema(description = "需要更新置顶的文章ID列表", example = "[1, 2, 3]")
    private List<Long> articleIds;

    @NotNull(message = "状态类型不能为空")
    @Schema(description = "1-置顶，0-取消置顶", example = "1")
    private ToppingStatusEnum toppingStat;
}