package top.harrylei.forum.api.model.ai.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * AI使用统计VO
 *
 * @author harry
 */
@Data
@Schema(description = "AI使用统计")
public class AIUsageStatsVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "统计日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @Schema(description = "消息数量")
    private Integer messageCount;

    @Schema(description = "Token消耗量")
    private Integer tokensUsed;

    @Schema(description = "对话数量")
    private Integer conversationCount;

    @Schema(description = "每日消息限制")
    private Integer dailyMessageLimit;

    @Schema(description = "剩余消息数量")
    private Integer remainingMessages;

    @Schema(description = "每日Token限制")
    private Integer dailyTokenLimit;

    @Schema(description = "剩余Token数量")
    private Integer remainingTokens;
}