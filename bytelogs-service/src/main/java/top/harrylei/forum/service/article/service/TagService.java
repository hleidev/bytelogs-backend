package top.harrylei.forum.service.article.service;

import top.harrylei.forum.api.model.vo.article.dto.TagDTO;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.api.model.vo.page.param.TagQueryParam;

/**
 * 标签服务接口类
 */
public interface TagService {

    /**
     * 新建标签
     *
     * @param tag 标签传输对象
     */
    void save(TagDTO tag);

    /**
     * 标签分页查询
     *
     * @param queryParam 标签及筛选参数
     * @return 标签分类列表
     */
    PageVO<TagDTO> page(TagQueryParam queryParam);
}
