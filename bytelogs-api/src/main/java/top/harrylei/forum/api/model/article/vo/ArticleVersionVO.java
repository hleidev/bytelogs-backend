package top.harrylei.forum.api.model.article.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.forum.api.model.base.BaseVO;
import top.harrylei.forum.api.enums.YesOrNoEnum;
import top.harrylei.forum.api.enums.article.PublishStatusEnum;

import java.time.LocalDateTime;

/**
 * 文章版本历史条目VO
 *
 * @author harry
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "文章版本历史条目")
public class ArticleVersionVO extends BaseVO {

    /**
     * 版本号
     */
    @Schema(description = "版本号", example = "3")
    private Integer version;

    /**
     * 文章标题
     */
    @Schema(description = "文章标题", example = "Spring Boot 实战指南")
    private String title;

    /**
     * 短标题
     */
    @Schema(description = "短标题", example = "Spring Boot 指南")
    private String shortTitle;

    /**
     * 版本状态
     */
    @Schema(description = "版本状态")
    private PublishStatusEnum status;

    /**
     * 是否为最新版本
     */
    @Schema(description = "是否为最新版本")
    private YesOrNoEnum latest;

    /**
     * 是否为发布版本
     */
    @Schema(description = "是否为发布版本")
    private YesOrNoEnum published;

    /**
     * 发布时间
     */
    @Schema(description = "发布时间")
    private LocalDateTime publishTime;
}