package top.harrylei.forum.api.model.vo.article.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.harrylei.forum.api.model.entity.BaseVO;

/**
 * 管理端标签详情展示对象
 */
@Data
@Schema(description = "标签详情对象")
public class TagDetailVO extends BaseVO {

    /**
     * 标签名称
     */
    @Schema(description = "标签名称", example = "Java")
    private String tagName;

    /**
     * 标签类型：1-系统标签，2-自定义标签
     */
    @Schema(description = "标签类型：1-系统标签，2-自定义标签", example = "1")
    private Integer tagType;

    /**
     * 标签类型：1-系统标签，2-自定义标签
     */
    @Schema(description = "标签类型：1-系统标签，2-自定义标签", example = "1")
    private String tagTypeLabel;

    /**
     * 所属分类ID
     */
    @Schema(description = "所属分类ID", example = "10")
    private Long categoryId;

    /**
     * 标签状态：0-未发布，1-已发布
     */
    @Schema(description = "标签状态：0-未发布，1-已发布", example = "1")
    private Integer status;

    /**
     * 标签状态：0-未发布，1-已发布
     */
    @Schema(description = "标签状态：0-未发布，1-已发布", example = "1")
    private String statusLabel;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @Schema(description = "是否删除：0-未删除，1-已删除", example = "0")
    private Integer deleted;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @Schema(description = "是否删除：0-未删除，1-已删除", example = "0")
    private String deletedLabel;
}