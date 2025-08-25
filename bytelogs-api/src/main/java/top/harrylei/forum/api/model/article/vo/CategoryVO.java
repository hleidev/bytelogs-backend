package top.harrylei.forum.api.model.article.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.forum.api.model.base.BaseVO;

/**
 * 管理端分类详细展示对象
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "分类展示对象")
public class CategoryVO extends BaseVO {

    /**
     * 分类名
     */
    @Schema(description = "分类名", example = "笔记")
    private String categoryName;


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
