package top.harrylei.forum.service.article.service;

import top.harrylei.forum.api.model.vo.article.req.TagReq;

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
}
