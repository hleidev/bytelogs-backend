package top.harrylei.forum.service.category.service;

import top.harrylei.forum.api.model.vo.article.CategoryCreateReq;

/**
 * 分类服务接口类
 */
public interface CategoryService {

    /**
     * 保存分类
     *
     * @param req 新建分类的请求参数
     */
    void save(CategoryCreateReq req);
}
