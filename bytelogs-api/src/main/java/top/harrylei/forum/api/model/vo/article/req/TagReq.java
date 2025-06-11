package top.harrylei.forum.api.model.vo.article.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import top.harrylei.forum.api.model.enums.PushStatusEnum;

/**
 * 新建标签请求参数
 */
@Data
@Schema(description = "标签请求")
public class TagReq {

    /**
     * 标签名称
     */
    @NotBlank(message = "标签名称不能为空")
    @Size(min = 2, max = 120, message = "标签名称长度为2-120个字符")
    @Pattern(
            regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_-]{2,120}$",
            message = "标签名称可包含中文、字母、数字、下划线和短横线"
    )
    @Schema(description = "标签名称", example = "Java", requiredMode = Schema.RequiredMode.REQUIRED)
    private String tagName;

    /**
     * 标签类型：1-系统标签，2-自定义标签
     */
    @NotNull(message = "标签类型不能为空")
    @Min(value = 1, message = "标签类型最小为1")
    @Max(value = 2, message = "标签类型最大为2")
    @Schema(description = "标签类型，1-系统标签，2-自定义标签", example = "1")
    private Integer tagType;

    /**
     * 类目ID
     */
    @NotNull(message = "类目ID不能为空")
    @Schema(description = "类目ID", example = "1")
    private Long categoryId;

    /**
     * 状态：0-未发布，1-已发布
     */
    @NotNull(message = "标签状态不能为空")
    @Schema(description = "标签状态", example = "0")
    private PushStatusEnum status;
}