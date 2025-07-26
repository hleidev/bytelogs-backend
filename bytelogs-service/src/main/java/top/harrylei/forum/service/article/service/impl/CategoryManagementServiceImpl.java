package top.harrylei.forum.service.article.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.enums.article.PublishStatusEnum;
import top.harrylei.forum.api.enums.ErrorCodeEnum;
import top.harrylei.forum.api.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.article.req.CategoryReq;
import top.harrylei.forum.api.model.article.dto.CategoryDTO;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.service.article.converted.CategoryStructMapper;
import top.harrylei.forum.service.article.service.CategoryManagementService;
import top.harrylei.forum.service.article.service.CategoryService;

import java.util.List;

/**
 * 分类管理服务实现类
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryManagementServiceImpl implements CategoryManagementService {

    private final CategoryService categoryService;
    private final CategoryStructMapper categoryStructMapper;

    /**
     * 新建分类
     *
     * @param req 新建分类的请求参数
     */
    @Override
    public void save(CategoryReq req) {
        ExceptionUtil.requireValid(req, ErrorCodeEnum.PARAM_MISSING, "分类请求参数");

        categoryService.save(req);
    }

    /**
     * 修改分类
     *
     * @param categoryId 分类ID
     * @param req        修改参数
     */
    @Override
    public CategoryDTO update(Long categoryId, CategoryReq req) {
        ExceptionUtil.requireValid(categoryId, ErrorCodeEnum.PARAM_MISSING, "分类ID");
        ExceptionUtil.requireValid(req, ErrorCodeEnum.PARAM_MISSING, "分类请求参数");

        CategoryDTO category = categoryStructMapper.toDTO(req);
        category.setId(categoryId);
        return categoryService.update(category);
    }

    /**
     * 修改分类状态
     *
     * @param categoryId 分类ID
     * @param status     新状态
     */
    @Override
    public void updateStatus(Long categoryId, PublishStatusEnum status) {
        ExceptionUtil.requireValid(categoryId, ErrorCodeEnum.PARAM_MISSING, "分类ID");
        ExceptionUtil.requireValid(status, ErrorCodeEnum.PARAM_MISSING, "分类状态");

        categoryService.updateStatus(categoryId, status);
    }

    /**
     * 删除分类
     *
     * @param categoryId 分类ID
     */
    @Override
    public void delete(Long categoryId) {
        ExceptionUtil.requireValid(categoryId, ErrorCodeEnum.PARAM_MISSING, "分类ID");

        categoryService.updateDeleted(categoryId, YesOrNoEnum.YES);
    }

    /**
     * 恢复分类
     *
     * @param categoryId 分类ID
     */
    @Override
    public void restore(Long categoryId) {
        ExceptionUtil.requireValid(categoryId, ErrorCodeEnum.PARAM_MISSING, "分类ID");

        categoryService.updateDeleted(categoryId, YesOrNoEnum.NO);
    }

    /**
     * 已删分类
     *
     * @return 已删分类列表
     */
    @Override
    public List<CategoryDTO> listDeleted() {
        return categoryService.listDeleted();
    }
}
