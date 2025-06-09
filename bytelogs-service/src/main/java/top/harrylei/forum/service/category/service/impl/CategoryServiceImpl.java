package top.harrylei.forum.service.category.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.StatusEnum;
import top.harrylei.forum.api.model.vo.article.CategoryCreateReq;
import top.harrylei.forum.core.exception.ExceptionUtil;
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

    /**
     * 新建分类
     *
     * @param req 新建分类的请求参数
     */
    @Override
    public void save(CategoryCreateReq req) {
        ExceptionUtil.requireNonNull(req, StatusEnum.PARAM_MISSING, "分类请求参数");

        CategoryDO category = new CategoryDO()
                .setCategoryName(req.getCategoryName())
                .setStatus(req.getStatus() == 1 ? 1 : 0)
                .setSort(Optional.ofNullable(req.getSort()).orElse(0));

        categoryDAO.save(category);
    }
}
