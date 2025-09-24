package top.harrylei.community.api.model.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.harrylei.community.api.enums.ai.ChatClientTypeEnum;

/**
 * AI聊天结果封装类 - 增强以支持多厂商信息
 *
 * @author harry
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResult {
    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "使用的模型名称")
    private String model;

    @Schema(description = "AI提供商")
    private ChatClientTypeEnum provider;

    @Schema(description = "提示词Token数")
    private Long promptTokens;

    @Schema(description = "完成Token数")
    private Long completionTokens;

    @Schema(description = "总Token数")
    private Long totalTokens;
}