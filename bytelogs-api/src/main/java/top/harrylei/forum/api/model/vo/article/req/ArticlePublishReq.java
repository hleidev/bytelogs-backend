package top.harrylei.forum.api.model.vo.article.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;

import java.time.LocalDateTime;

/**
 * 文章发布请求
 *
 * @author harry
 */
@Data
@Schema(description = "文章发布请求")
public class ArticlePublishReq {


    /**
     * 版本号
     */
    @NotNull(message = "版本号不能为空")
    @Schema(description = "版本号", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long version;

    /**
     * 是否立即发布
     */
    @NotNull(message = "发布类型不能为空")
    @Schema(description = "是否立即发布，1-立即发布，0-定时发布", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private YesOrNoEnum immediate;

    /**
     * 定时发布时间
     */
    @Schema(description = "定时发布时间（定时发布时必填）", example = "2024-01-01 12:00:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledTime;
}