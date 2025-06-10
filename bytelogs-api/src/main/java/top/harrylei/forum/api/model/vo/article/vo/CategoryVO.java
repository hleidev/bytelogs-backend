package top.harrylei.forum.api.model.vo.article.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分类展示对象
 */
@Data
@Schema(description = "分类展示对象")
public class CategoryVO {

    /**
     * 分类名
     */
    @Schema(description = "分类名", example = "笔记")
    private String categoryName;

    /**
     * 分类状态
     */
    @Schema(description = "分类状态", example = "1")
    private Integer status;

    /**
     * 分类排序
     */
    @Schema(description = "分类排序", example = "10")
    private Integer sort;

    /**
     * 删除标识
     */
    @Schema(description = "是否删除", example = "0")
    private Integer deleted;
}
