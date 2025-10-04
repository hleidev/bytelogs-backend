package top.harrylei.community.api.model.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分类简单传输对象
 *
 * @author harry
 */
@Data
@Schema(description = "分类简单传输对象")
public class CategorySimpleDTO {

    /**
     * 分类主键
     */
    @Schema(description = "分类主键", example = "1")
    private Long categoryId;

    /**
     * 分类名
     */
    @Schema(description = "分类名", example = "笔记")
    private String categoryName;
}
