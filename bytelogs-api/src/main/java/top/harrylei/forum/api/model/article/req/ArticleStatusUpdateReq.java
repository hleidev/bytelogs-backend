package top.harrylei.forum.api.model.article.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.harrylei.forum.api.enums.YesOrNoEnum;
import top.harrylei.forum.api.enums.article.ArticleStatusTypeEnum;

import java.util.List;

/**
 * 文章状态更新请求对象
 *
 * @author harry
 */
@Data
@Schema(description = "文章状态更新请求")
public class ArticleStatusUpdateReq {

    @NotEmpty(message = "文章ID列表不能为空")
    @Schema(description = "需要更新状态的文章ID列表", example = "[1, 2, 3]")
    private List<Long> articleIds;

    @NotNull(message = "状态类型不能为空")
    @Schema(description = "状态类型：1-置顶，2-加精，3-官方", example = "1")
    private ArticleStatusTypeEnum statusType;

    @NotNull(message = "状态值不能为空")
    @Schema(description = "状态值：1-设置，0-取消", example = "1")
    private YesOrNoEnum status;
}