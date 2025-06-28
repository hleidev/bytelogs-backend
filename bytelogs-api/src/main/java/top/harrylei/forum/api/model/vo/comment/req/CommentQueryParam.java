package top.harrylei.forum.api.model.vo.comment.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.forum.api.model.entity.BasePage;

/**
 * 评论分页查询参数
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "评论分页查询参数")
public class CommentQueryParam extends BasePage {

    @NotNull(message = "文章ID不能为空")
    @Schema(description = "文章ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    public Long articleId;
}