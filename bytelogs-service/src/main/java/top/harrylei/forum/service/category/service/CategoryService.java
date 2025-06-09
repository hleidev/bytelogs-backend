package top.harrylei.forum.service.category.service;

import top.harrylei.forum.api.model.vo.article.CategoryReq;

/**
 * 分类服务接口类
 */
public interface CategoryService {

    /**
     * 保存分类
     *
     * @param req 新建分类的请求参数
     */
    void save(CategoryReq req);

    /**
     * 更新分类
     *
     * @param categoryId 分类ID
     * @param req 修改参数
     */
    void update(Long categoryId, CategoryReq req);
}
