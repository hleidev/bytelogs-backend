package top.harrylei.forum.api.model.article.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import top.harrylei.forum.api.model.base.BaseDTO;
import top.harrylei.forum.api.enums.article.PublishStatusEnum;

/**
 * 分类传输对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO extends BaseDTO {

    /**
     * 类目名称
     */
    private String categoryName;

    /**
     * 状态：0-未发布，1-已发布
     */
    private PublishStatusEnum status;

    /**
     * 排序值（越大越靠前）
     */
    private Integer sort;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    private Integer deleted;
}
