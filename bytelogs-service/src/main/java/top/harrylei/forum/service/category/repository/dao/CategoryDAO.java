package top.harrylei.forum.service.category.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.service.category.repository.entity.CategoryDO;
import top.harrylei.forum.service.category.repository.mapper.CategoryMapper;

/**
 * 分类访问对象
 */
@Repository
public class CategoryDAO extends ServiceImpl<CategoryMapper, CategoryDO> {


    public CategoryDO getByCategoryId(Long categoryId) {
        return lambdaQuery()
                .eq(CategoryDO::getId, categoryId)
                .eq(CategoryDO::getDeleted, YesOrNoEnum.NO.getCode())
                .one();
    }
}
