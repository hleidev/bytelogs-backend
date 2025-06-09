package top.harrylei.forum.service.admin.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.StatusEnum;
import top.harrylei.forum.api.model.vo.article.CategoryReq;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.service.admin.service.CategoryManagementService;
import top.harrylei.forum.service.category.service.CategoryService;

/**
 * 分类管理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryManagementServiceImpl implements CategoryManagementService {

    private final CategoryService categoryService;

    /**
     * 新建分类
     *
     * @param req 新建分类的请求参数
     */
    @Override
    public void save(CategoryReq req) {
        ExceptionUtil.requireNonNull(req, StatusEnum.PARAM_MISSING, "分类请求参数");

        categoryService.save(req);
    }

    /**
     * 修改分类
     *
     * @param categoryId 分类ID
     * @param req 修改参数
     */
    @Override
    public void update(Long categoryId, CategoryReq req) {
        ExceptionUtil.requireNonNull(categoryId, StatusEnum.PARAM_MISSING, "分类ID");
        ExceptionUtil.requireNonNull(req, StatusEnum.PARAM_MISSING, "分类请求参数");

        categoryService.update(categoryId, req);
    }
}
