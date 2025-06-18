package top.harrylei.forum.service.article.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.forum.api.model.enums.article.PublishStatusEnum;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.vo.article.vo.CategorySimpleVO;
import top.harrylei.forum.api.model.vo.page.param.CategoryQueryParam;
import top.harrylei.forum.service.article.repository.entity.CategoryDO;
import top.harrylei.forum.service.article.repository.mapper.CategoryMapper;

import java.util.List;

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

    public List<CategoryDO> listCategory(CategoryQueryParam queryParam, String limitSql) {
        String orderBySql = queryParam.getOrderBySql();
        return lambdaQuery()
                .like(queryParam.getCategoryName() != null && !queryParam.getCategoryName().isEmpty(),
                        CategoryDO::getCategoryName, queryParam.getCategoryName())
                .eq(queryParam.getStatus() != null, CategoryDO::getStatus, queryParam.getStatus())
                .eq(queryParam.getSortWeight() != null, CategoryDO::getSort, queryParam.getSortWeight())
                .eq(CategoryDO::getDeleted, YesOrNoEnum.NO.getCode())
                .last(orderBySql + " " + limitSql)
                .list();
    }

    public long countCategory(CategoryQueryParam queryParam) {
        return lambdaQuery()
                .like(queryParam.getCategoryName() != null && !queryParam.getCategoryName().isEmpty(),
                        CategoryDO::getCategoryName, queryParam.getCategoryName())
                .eq(queryParam.getStatus() != null, CategoryDO::getStatus, queryParam.getStatus())
                .eq(queryParam.getSortWeight() != null, CategoryDO::getSort, queryParam.getSortWeight())
                .eq(CategoryDO::getDeleted, YesOrNoEnum.NO.getCode())
                .count();
    }

    public List<CategoryDO> getDeleted() {
        return lambdaQuery()
                .eq(CategoryDO::getDeleted, YesOrNoEnum.YES.getCode())
                .list();
    }

    public List<CategoryDO> listPublishedAndUndeleted() {
        return lambdaQuery()
                .eq(CategoryDO::getStatus, PublishStatusEnum.PUBLISHED.getCode())
                .eq(CategoryDO::getDeleted, YesOrNoEnum.NO.getCode())
                .list();
    }

    public CategorySimpleVO getSimpleById(Long id) {
        return this.getBaseMapper().getSimpleById(id);
    }
}
