package top.harrylei.community.service.article.repository.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.community.api.enums.YesOrNoEnum;
import top.harrylei.community.api.model.page.param.CategoryQueryParam;
import top.harrylei.community.service.article.repository.entity.CategoryDO;
import top.harrylei.community.service.article.repository.mapper.CategoryMapper;

import java.util.List;

/**
 * 分类访问对象
 *
 * @author harry
 */
@Repository
public class CategoryDAO extends ServiceImpl<CategoryMapper, CategoryDO> {


    public CategoryDO getByCategoryId(Long categoryId) {
        return lambdaQuery()
                .eq(CategoryDO::getId, categoryId)
                .eq(CategoryDO::getDeleted, YesOrNoEnum.NO)
                .one();
    }

    public IPage<CategoryDO> pageQuery(CategoryQueryParam queryParam, IPage<CategoryDO> page) {
        return lambdaQuery()
                .like(queryParam.getCategoryName() != null && !queryParam.getCategoryName().isEmpty(),
                        CategoryDO::getCategoryName, queryParam.getCategoryName())
                .eq(queryParam.getSortWeight() != null, CategoryDO::getSort, queryParam.getSortWeight())
                .ge(queryParam.getStartTime() != null, CategoryDO::getCreateTime, queryParam.getStartTime())
                .le(queryParam.getEndTime() != null, CategoryDO::getCreateTime, queryParam.getEndTime())
                .eq(CategoryDO::getDeleted, YesOrNoEnum.NO)
                .page(page);
    }

    public List<CategoryDO> listCategory(boolean deleted) {
        YesOrNoEnum yesOrNoEnum = deleted ? YesOrNoEnum.YES : YesOrNoEnum.NO;
        return lambdaQuery()
                .eq(CategoryDO::getDeleted, yesOrNoEnum)
                .list();
    }
}
