package top.harrylei.forum.api.model.article.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 文章编辑响应对象
 *
 * @author harry
 */
@Data
@Schema(description = "文章编辑响应对象")
@Accessors(chain = true)
public class ArticleEditVO {

    /**
     * 编辑权限过期时间
     */
    @Schema(description = "编辑权限过期时间", example = "2024-01-01 15:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;

    /**
     * 文章信息
     */
    @Schema(description = "文章信息", requiredMode = Schema.RequiredMode.REQUIRED)
    private ArticleVO article;
}