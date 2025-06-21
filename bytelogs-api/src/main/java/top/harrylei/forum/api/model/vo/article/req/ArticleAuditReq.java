package top.harrylei.forum.api.model.vo.article.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.harrylei.forum.api.model.enums.article.PublishStatusEnum;

/**
 * 文章审核请求
 *
 * @author Harry
 */
@Data
@Schema(description = "文章审核请求")
public class ArticleAuditReq {

    /**
     * 审核后的状态
     */
    @Schema(description = "审核后的状态：1-通过发布，3-驳回", example = "1")
    @NotNull(message = "审核状态不能为空")
    private PublishStatusEnum status;

    /**
     * 审核原因（可选，暂时不保存到数据库）
     */
    @Schema(description = "审核原因", example = "内容质量很好，审核通过")
    private String reason;

    /**
     * 验证审核状态是否有效
     */
    public boolean isValidAuditStatus() {
        return status == PublishStatusEnum.PUBLISHED || status == PublishStatusEnum.REJECTED;
    }
}