package top.harrylei.forum.service.article.service;

import top.harrylei.forum.api.model.vo.article.dto.TagDTO;
import top.harrylei.forum.api.model.vo.article.req.TagReq;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.api.model.vo.page.param.TagQueryParam;

/**
 * 标签管理接口类
 */
public interface TagManagementService {

    /**
     * 新建标签
     * 
     * @param tagReq 参数
     */
    void save(TagReq tagReq);

    /**
     * 标签分页查询
     *
     * @param queryParam 标签及筛选参数
     * @return 标签分类列表
     */
    PageVO<TagDTO> page(TagQueryParam queryParam);

    /**
     * 编辑标签
     *
     * @param tagId 标签ID
     * @param tagReq 标签编辑请求
     * @return 标签详细信息
     */
    TagDTO update(Long tagId, TagReq tagReq);

    /**
     * 删除标签
     *
     * @param tagId 标签ID
     */
    void delete(Long tagId);
}
