package top.harrylei.community.service.statistics.service;

import top.harrylei.community.api.enums.article.ContentTypeEnum;

/**
 * 阅读统计服务接口
 *
 * @author harry
 */
public interface ReadCountService {

    /**
     * 增加阅读量
     *
     * @param contentId   内容ID
     * @param contentType 内容类型
     */
    void incrementReadCount(Long contentId, ContentTypeEnum contentType);

    /**
     * 获取阅读量
     *
     * @param contentId   内容ID
     * @param contentType 内容类型
     * @return 阅读量
     */
    Long getReadCount(Long contentId, ContentTypeEnum contentType);
}