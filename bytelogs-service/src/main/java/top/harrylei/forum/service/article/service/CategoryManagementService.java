package top.harrylei.forum.service.article.service;

import top.harrylei.forum.api.model.enums.article.PublishStatusEnum;
import top.harrylei.forum.api.model.vo.article.dto.CategoryDTO;
import top.harrylei.forum.api.model.vo.article.req.CategoryReq;

import java.util.List;

/**
 * 分类管理服务接口类
 */
public interface CategoryManagementService {

    /**
     * 新建分类
     *
     * @param req 新建分类的请求参数
     */
    void save(CategoryReq req);

    /**
     * 修改分类
     *
     * @param categoryId 分类ID
     * @param req 修改参数
     */
    CategoryDTO update(Long categoryId, CategoryReq req);

    /**
     * 修改分类状态
     *
     * @param categoryId 分类ID
     * @param status 新状态
     */
    void updateStatus(Long categoryId, PublishStatusEnum status);

    /**
     * 删除分类
     *
     * @param categoryId 分类ID
     */
    void delete(Long categoryId);

    /**
     * 恢复分类
     *
     * @param categoryId 分类ID
     */
    void restore(Long categoryId);

    /**
     * 已删分类
     *
     * @return 已删分类列表
     */
    List<CategoryDTO> listDeleted();
}
