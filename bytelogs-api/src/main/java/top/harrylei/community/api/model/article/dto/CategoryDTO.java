package top.harrylei.community.api.model.article.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import top.harrylei.community.api.model.base.BaseDTO;
import top.harrylei.community.api.enums.YesOrNoEnum;

/**
 * 分类传输对象
 *
 * @author harry
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
     * 排序值（越大越靠前）
     */
    private Integer sort;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    private YesOrNoEnum deleted;
}
