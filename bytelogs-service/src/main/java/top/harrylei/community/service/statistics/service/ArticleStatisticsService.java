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

    /**
     * 增加文章阅读量
     *
     * @param articleId 文章ID
     */
    void incrementReadCount(Long articleId);

    /**
     * 增加文章点赞量
     *
     * @param articleId 文章ID
     */
    void incrementPraiseCount(Long articleId);

    /**
     * 减少文章点赞量
     *
     * @param articleId 文章ID
     */
    void decrementPraiseCount(Long articleId);

    /**
     * 增加文章收藏量
     *
     * @param articleId 文章ID
     */
    void incrementCollectCount(Long articleId);

    /**
     * 减少文章收藏量
     *
     * @param articleId 文章ID
     */
    void decrementCollectCount(Long articleId);

    /**
     * 增加文章评论量
     *
     * @param articleId 文章ID
     */
    void incrementCommentCount(Long articleId);

    /**
     * 减少文章评论量
     *
     * @param articleId 文章ID
     */
    void decrementCommentCount(Long articleId);
}