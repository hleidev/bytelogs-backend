package top.harrylei.forum.api.model.vo.article.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分类展示对象
 */
@Data
@Schema(description = "分类展示对象")
public class CategorySimpleVO {

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
