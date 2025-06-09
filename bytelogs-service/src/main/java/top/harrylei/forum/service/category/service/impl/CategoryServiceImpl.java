package top.harrylei.forum.service.category.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.StatusEnum;
import top.harrylei.forum.api.model.vo.article.CategoryReq;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.service.category.converted.CategoryStructMapper;
import top.harrylei.forum.service.category.repository.dao.CategoryDAO;
import top.harrylei.forum.service.category.repository.entity.CategoryDO;
import top.harrylei.forum.service.category.service.CategoryService;

/**
 * 分类服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryDAO categoryDAO;
    private final CategoryStructMapper categoryStructMapper;

    /**
     * 新建分类
     *
     * @param req 新建分类的请求参数
     */
    @Override
    public void save(CategoryReq req) {
        ExceptionUtil.requireNonNull(req, StatusEnum.PARAM_MISSING, "分类请求参数");

        CategoryDO category = new CategoryDO()
                .setCategoryName(req.getCategoryName())
                .setStatus(req.getStatus())
                .setSort(req.getSort());

        try {
            categoryDAO.save(category);
            log.info("新建分类成功 category={}", category.getCategoryName());
        } catch (Exception e) {
            ExceptionUtil.error(StatusEnum.SYSTEM_ERROR, "新建分类失败", e);
        }
    }

    /**
     * 更新分类
     *
     * @param categoryId 分类ID
     * @param req 修改参数
     */
    @Override
    public void update(Long categoryId, CategoryReq req) {
        ExceptionUtil.requireNonNull(categoryId, StatusEnum.PARAM_MISSING, "分类ID");
        ExceptionUtil.requireNonNull(req, StatusEnum.PARAM_MISSING, "分类请求参数");

        CategoryDO category = categoryDAO.getByCategoryId(categoryId);
        ExceptionUtil.requireNonNull(category, StatusEnum.CATEGORY_NOT_EXISTS);

        categoryStructMapper.updateDOFromReq(req, category);

        try {
            categoryDAO.updateById(category);
        } catch (Exception e) {
            ExceptionUtil.error(StatusEnum.CATEGORY_UPDATE_FAILED, "新建分类失败", e);
        }
    }
}
