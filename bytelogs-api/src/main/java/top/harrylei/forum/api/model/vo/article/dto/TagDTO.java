package top.harrylei.forum.api.model.vo.article.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import top.harrylei.forum.api.model.entity.BaseDTO;
import top.harrylei.forum.api.model.enums.PublishStatusEnum;

/**
 * 标签传输对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagDTO extends BaseDTO {

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 标签类型：1-系统标签，2-自定义标签
     */
    private Integer tagType;

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
    private Integer deleted;
}