package top.harrylei.community.api.model.article.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import top.harrylei.community.api.enums.article.TagTypeEnum;

/**
 * 新建标签请求参数
 *
 * @author harry
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
     * 标签类型：1-系统标签，2-用户标签
     */
    @NotNull(message = "标签类型不能为空")
    @Schema(description = "标签类型，1-系统标签，2-自定义标签", example = "1")
    private TagTypeEnum tagType;
}