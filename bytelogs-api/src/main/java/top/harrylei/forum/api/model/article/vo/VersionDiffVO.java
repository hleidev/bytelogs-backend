package top.harrylei.forum.api.model.article.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 版本对比结果VO
 *
 * @author harry
 */
@Data
@Accessors(chain = true)
@Schema(description = "版本对比结果")
public class VersionDiffVO {

    /**
     * 版本1信息
     */
    @Schema(description = "版本1信息")
    private ArticleVersionVO version1;

    /**
     * 版本2信息
     */
    @Schema(description = "版本2信息")
    private ArticleVersionVO version2;

    /**
     * 标题差异（HTML格式，标记增删改）
     */
    @Schema(description = "标题差异（HTML格式）", example = "Spring Boot <span class='diff-insert'>3.0</span> 实战指南")
    private String titleDiff;

    /**
     * 内容差异（HTML格式，标记增删改）
     */
    @Schema(description = "内容差异（HTML格式）")
    private String contentDiff;

    /**
     * 摘要差异
     */
    @Schema(description = "摘要差异（HTML格式）")
    private String summaryDiff;
}