package top.harrylei.forum.service.article.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.forum.api.model.base.BaseDO;

import java.io.Serial;

/**
 * 分类实体对象
 *
 * @author harry
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("category")
@Accessors(chain = true)
public class CategoryDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

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
    private Integer deleted;
}
