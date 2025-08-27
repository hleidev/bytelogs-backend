package top.harrylei.forum.api.model.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.harrylei.forum.api.enums.ai.ChatClientTypeEnum;

/**
 * AI聊天结果封装类 - 增强以支持多厂商信息
 */
@Data
public class ChatResult {
    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "使用的模型名称")
    private String model;

    @Schema(description = "AI提供商")
    private ChatClientTypeEnum provider;

    @Schema(description = "提示词Token数")
    private Long promptTokens = 0L;

    @Schema(description = "完成Token数")
    private Long completionTokens = 0L;

    @Schema(description = "总Token数")
    private Long totalTokens = 0L;
}