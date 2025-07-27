package top.harrylei.forum.api.model.comment.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.harrylei.forum.api.enums.user.OperateTypeEnum;

/**
 * 评论操作请求
 *
 * @author harry
 */
@Data
@Schema(description = "评论操作请求")
public class CommentActionReq {

    @Schema(description = "评论ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "评论ID不能为空")
    private Long commentId;

    @Schema(description = "操作类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "操作类型不能为空")
    private OperateTypeEnum type;
}