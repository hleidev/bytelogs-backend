package top.harrylei.community.api.model.article.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.harrylei.community.api.enums.article.TagTypeEnum;

/**
 * 标签展示对象
 *
 * @author harry
 */
@Data
@Schema(description = "标签展示对象")
public class TagSimpleVO {

    /**
     * 文章ID（用于批量查询时的关联，前端不显示）
     */
    @Schema(description = "文章ID", example = "1", hidden = true)
    @JsonIgnore
    private Long articleId;

    /**
     * 标签主键
     */
    @Schema(description = "标签主键", example = "1")
    private Long tagId;

    /**
     * 标签名称
     */
    @Schema(description = "标签名称", example = "Java")
    private String tagName;

    /**
     * 标签类型：1-系统标签，2-自定义标签
     */
    @Schema(description = "标签类型", example = "{\"code\":1,\"label\":\"系统标签\"}")
    private TagTypeEnum tagType;
}