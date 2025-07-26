package top.harrylei.forum.service.article.repository.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.forum.api.enums.YesOrNoEnum;
import top.harrylei.forum.api.enums.article.PublishStatusEnum;
import top.harrylei.forum.api.model.page.param.CategoryQueryParam;
import top.harrylei.forum.service.article.repository.entity.CategoryDO;
import top.harrylei.forum.service.article.repository.mapper.CategoryMapper;

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
                .eq(CategoryDO::getDeleted, YesOrNoEnum.NO.getCode())
                .one();
    }

    public IPage<CategoryDO> pageQuery(CategoryQueryParam queryParam, IPage<CategoryDO> page) {
        return lambdaQuery()
                .like(queryParam.getCategoryName() != null && !queryParam.getCategoryName().isEmpty(),
                      CategoryDO::getCategoryName, queryParam.getCategoryName())
                .eq(queryParam.getStatus() != null, CategoryDO::getStatus, queryParam.getStatus())
                .eq(queryParam.getSortWeight() != null, CategoryDO::getSort, queryParam.getSortWeight())
                .ge(queryParam.getStartTime() != null, CategoryDO::getCreateTime, queryParam.getStartTime())
                .le(queryParam.getEndTime() != null, CategoryDO::getCreateTime, queryParam.getEndTime())
                .eq(CategoryDO::getDeleted, YesOrNoEnum.NO.getCode())
                .page(page);
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
}
