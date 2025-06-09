package top.harrylei.forum.service.admin.service;

import jakarta.validation.Valid;
import top.harrylei.forum.api.model.vo.article.CategoryCreateReq;

/**
 * 分类管理服务接口类
 */
public interface CategoryManagementService {

    /**
     * 新建分类
     *
     * @param req 新建分类的请求参数
     */
    void save(@Valid CategoryCreateReq req);
}
