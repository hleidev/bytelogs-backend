package top.harrylei.forum.api.model.vo.article.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 版本对比请求参数
 *
 * @author harry
 */
@Data
@Schema(description = "版本对比请求参数")
public class VersionCompareReq {

    /**
     * 版本1
     */
    @Schema(description = "版本1", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "版本1不能为空")
    @Min(value = 1, message = "版本号必须大于0")
    private Integer version1;

    /**
     * 版本2
     */
    @Schema(description = "版本2", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "版本2不能为空")
    @Min(value = 1, message = "版本号必须大于0")
    private Integer version2;
}