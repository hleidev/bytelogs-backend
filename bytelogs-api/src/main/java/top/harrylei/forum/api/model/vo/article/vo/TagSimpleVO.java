package top.harrylei.forum.api.model.vo.article.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 标签展示对象
 */
@Data
@Schema(description = "标签展示对象")
public class TagSimpleVO {

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
    @Schema(description = "标签编码", example = "0")
    private Integer tagType;

    /**
     * 标签描述
     */
    @Schema(description = "标签描述", example = "0")
    private String tagTypeLabel;
}