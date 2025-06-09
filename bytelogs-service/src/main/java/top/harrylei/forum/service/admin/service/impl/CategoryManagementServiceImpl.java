package top.harrylei.forum.service.admin.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.StatusEnum;
import top.harrylei.forum.api.model.vo.article.CategoryCreateReq;
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
    public void save(CategoryCreateReq req) {
        ExceptionUtil.requireNonNull(req, StatusEnum.PARAM_MISSING, "分类请求参数");

        categoryService.save(req);
    }
}
