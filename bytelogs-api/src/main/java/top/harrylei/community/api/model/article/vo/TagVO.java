package top.harrylei.community.api.model.article.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.community.api.model.base.BaseVO;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.api.enums.article.TagTypeEnum;

/**
 * 管理端标签详情展示对象
 *
 * @author harry
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
     * 标签类型：1-系统标签，2-用户标签
     */
    @Schema(description = "标签类型：1-系统标签，2-用户标签", example = "{\"code\":1,\"label\":\"系统标签\"}")
    private TagTypeEnum tagType;

    /**
     * 创建者ID
     */
    @Schema(description = "创建者ID，0-系统标签", example = "0")
    private Long creatorId;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @Schema(description = "是否删除：0-未删除，1-已删除", example = "{\"code\":0,\"label\":\"未删除\"}")
    private DeleteStatusEnum deleted;
}