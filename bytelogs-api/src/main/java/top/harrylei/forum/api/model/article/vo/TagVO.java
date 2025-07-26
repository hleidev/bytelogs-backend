package top.harrylei.forum.api.model.article.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.forum.api.model.base.BaseVO;
import top.harrylei.forum.api.enums.YesOrNoEnum;
import top.harrylei.forum.api.enums.article.PublishStatusEnum;
import top.harrylei.forum.api.enums.article.TagTypeEnum;

/**
 * 管理端标签详情展示对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "标签详情对象")
public class TagVO extends BaseVO {

    /**
     * 标签名称
     */
    @Schema(description = "标签名称", example = "Java")
    private String tagName;

    /**
     * 标签类型：1-系统标签，2-自定义标签
     */
    @Schema(description = "标签类型：1-系统标签，2-自定义标签", example = "{\"code\":1,\"label\":\"系统标签\"}")
    private TagTypeEnum tagType;

    /**
     * 所属分类ID
     */
    @Schema(description = "所属分类ID", example = "10")
    private Long categoryId;

    /**
     * 标签状态：0-未发布，1-已发布
     */
    @Schema(description = "标签状态：0-未发布，1-已发布", example = "{\"code\":1,\"label\":\"已发布\"}")
    private PublishStatusEnum status;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @Schema(description = "是否删除：0-未删除，1-已删除", example = "{\"code\":0,\"label\":\"未删除\"}")
    private YesOrNoEnum deleted;
}