package top.harrylei.community.api.model.ai.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.community.api.model.base.BasePage;

import java.time.LocalDateTime;

/**
 * AI消息查询参数
 *
 * @author harry
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MessagesQueryParam extends BasePage {

    @Schema(description = "获取此时间之前的消息，用于向上滚动加载历史", example = "2025-08-16 16:05:19")
    private LocalDateTime beforeTime;
}