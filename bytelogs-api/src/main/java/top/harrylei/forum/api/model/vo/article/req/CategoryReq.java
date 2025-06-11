package top.harrylei.forum.api.model.vo.article.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import top.harrylei.forum.api.model.enums.PublishStatusEnum;

/**
 * 新建分类请求参数
 */
@Data
@Schema(description = "分类请求")
public class CategoryReq {

    /**
     * 类目名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Pattern(
            regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_-]{2,16}$",
            message = "分类名称可包含中文、字母、数字、下划线和短横线，长度为2-16个字符"
    )
    @Schema(description = "分类名称", example = "编程", requiredMode = Schema.RequiredMode.REQUIRED)
    private String categoryName;

    /**
     * 状态：0-未发布，1-已发布
     */
    @NotNull(message = "分类状态不能为空")
    @Schema(description = "分类状态", example = "0")
    private PublishStatusEnum status;

    /**
     * 排序值（越大越靠前）
     */
    @NotNull(message = "排序值不能为空")
    @Min(value = 0, message = "排序值不能小于0")
    @Max(value = 255, message = "排序值不能大于255")
    @Schema(description = "排序值", example = "0")
    private Integer sort;
}