package top.harrylei.community.api.model.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import top.harrylei.community.api.enums.ai.ChatClientTypeEnum;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * AI提供商信息VO
 *
 * @author harry
 */
@Data
@Builder
@Schema(description = "AI提供商信息")
public class AiProviderInfoVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "提供商类型")
    private ChatClientTypeEnum provider;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "默认模型")
    private String defaultModel;

    @Schema(description = "支持的模型列表")
    private List<String> supportedModels;

    @Schema(description = "温度参数范围 [min, max]")
    private float[] temperatureRange;

    @Schema(description = "最大Token数限制")
    private Integer maxTokens;
}