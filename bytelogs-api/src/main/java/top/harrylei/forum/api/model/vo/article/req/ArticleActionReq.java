package top.harrylei.forum.api.model.vo.article.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.harrylei.forum.api.model.enums.OperateTypeEnum;

/**
 * 文章操作请求
 *
 * @author harry
 */
@Data
@Schema(description = "文章操作请求")
public class ArticleActionReq {

    @Schema(description = "文章ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    @Schema(description = "操作类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "操作类型不能为空")
    private OperateTypeEnum type;
}