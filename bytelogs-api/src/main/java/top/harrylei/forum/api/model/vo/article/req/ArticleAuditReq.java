package top.harrylei.forum.api.model.vo.article.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.harrylei.forum.api.model.enums.article.PublishStatusEnum;

import java.util.List;

/**
 * 文章审核请求对象
 *
 * @author Harry
 */
@Data
@Schema(description = "文章审核请求")
public class ArticleAuditReq {

    @NotNull(message = "文章ID列表不能为空")
    @Schema(description = "需要审核的文章ID列表", example = "[1, 2, 3]")
    private List<Long> articleIds;

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核后的状态：1-通过发布，3-驳回", example = "1")
    private PublishStatusEnum status;
}