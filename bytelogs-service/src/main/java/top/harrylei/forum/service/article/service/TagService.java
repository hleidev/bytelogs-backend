package top.harrylei.forum.service.article.service;

import top.harrylei.forum.api.model.vo.article.dto.TagDTO;

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
}
