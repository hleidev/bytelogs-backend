package top.harrylei.community.service.article.service;

import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.api.model.article.dto.CategoryDTO;
import top.harrylei.community.api.model.article.req.CategoryReq;
import top.harrylei.community.api.model.page.PageVO;
import top.harrylei.community.api.model.page.param.CategoryQueryParam;

import java.util.List;

/**
 * 分类服务接口类
 *
 * @author harry
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
     * @param categoryDTO 分类传输对象
     * @return 新的分类传输对象
     */
    CategoryDTO update(CategoryDTO categoryDTO);

    /**
     * 分类分页查询
     *
     * @param queryParam 分页及筛选参数
     * @return 分页分类列表
     */
    PageVO<CategoryDTO> pageQuery(CategoryQueryParam queryParam);

    /**
     * 更新删除状态
     *
     * @param categoryId 分类ID
     * @param status     删除状态
     */
    void updateDeleted(Long categoryId, DeleteStatusEnum status);

    /**
     * 分类列表
     *
     * @param deleted 是否查询已删除分类，true查询已删除，false查询未删除
     * @return 分类列表
     */
    List<CategoryDTO> listCategory(boolean deleted);
}
