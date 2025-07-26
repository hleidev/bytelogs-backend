package top.harrylei.forum.api.model.article.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.harrylei.forum.api.model.base.BaseDTO;
import top.harrylei.forum.api.enums.YesOrNoEnum;
import top.harrylei.forum.api.enums.article.PublishStatusEnum;
import top.harrylei.forum.api.enums.article.TagTypeEnum;

/**
 * 标签传输对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TagDTO extends BaseDTO {

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 标签类型：1-系统标签，2-自定义标签
     */
    private TagTypeEnum tagType;

    /**
     * 所属类目ID
     */
    private Long categoryId;

    /**
     * 状态：0-未发布，1-已发布
     */
    private PublishStatusEnum status;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    private YesOrNoEnum deleted;
}