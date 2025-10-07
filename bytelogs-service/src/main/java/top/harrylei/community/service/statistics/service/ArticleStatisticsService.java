package top.harrylei.community.service.statistics.service;

import top.harrylei.community.api.model.statistics.dto.ArticleStatisticsDTO;

/**
 * 文章统计服务接口
 *
 * @author harry
 */
public interface ArticleStatisticsService {

    /**
     * 获取文章统计数据
     *
     * @param articleId 文章ID
     * @return 文章统计数据
     */
    ArticleStatisticsDTO getArticleStatistics(Long articleId);

}