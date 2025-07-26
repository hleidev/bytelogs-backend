package top.harrylei.forum.api.model.comment.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.forum.api.model.base.BasePage;

/**
 * 评论分页查询参数（按文章查询）
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "按文章查询评论的分页参数")
public class CommentQueryParam extends BasePage {

    @Schema(description = "文章ID", example = "1")
    @NotNull(message = "文章ID不能为空")
    private Long articleId;
}