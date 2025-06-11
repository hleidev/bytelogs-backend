package top.harrylei.forum.api.model.vo.article.vo;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理端分类展示对象
 */
@Data
@Schema(description = "分类展示对象")
public class AdminCategoryVO {

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

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 最后编辑时间
     */
    @Schema(description = "最后编辑时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
