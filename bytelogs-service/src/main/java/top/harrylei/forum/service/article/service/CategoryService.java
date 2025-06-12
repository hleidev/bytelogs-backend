package top.harrylei.forum.service.article.service;

import top.harrylei.forum.api.model.enums.article.PublishStatusEnum;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.vo.article.req.CategoryReq;
import top.harrylei.forum.api.model.vo.article.dto.CategoryDTO;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.api.model.vo.page.param.CategoryQueryParam;

import java.util.List;

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
    PageVO<CategoryDTO> page(CategoryQueryParam queryParam);

    /**
     * 更新分类状态
     *
     * @param categoryId 分类ID
     * @param status 新状态
     */
    void updateStatus(Long categoryId, PublishStatusEnum status);

    /**
     * 更新删除状态
     *
     * @param categoryId 分类ID
     */
    void updateDeleted(Long categoryId, YesOrNoEnum status);

    /**
     * 已删分类
     *
     * @return 已删分类列表
     */
    List<CategoryDTO> listDeleted();

    /**
     * 分类列表
     *
     * @return 分类列表
     */
    List<CategoryDTO> list();
}
