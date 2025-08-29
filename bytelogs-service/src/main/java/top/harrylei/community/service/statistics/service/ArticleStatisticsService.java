package top.harrylei.community.service.statistics.service;

/**
 * 文章统计服务接口
 *
 * @author harry
 */
public interface ReadCountService {

    /**
     * 增加文章阅读量
     *
     * @param articleId 文章ID
     */
    void incrementReadCount(Long articleId);

    /**
     * 获取文章阅读量
     *
     * @param articleId 文章ID
     * @return 阅读量
     */
    Long getReadCount(Long articleId);

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
     * 获取文章点赞量
     *
     * @param articleId 文章ID
     * @return 点赞量
     */
    Long getPraiseCount(Long articleId);

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
     * 获取文章收藏量
     *
     * @param articleId 文章ID
     * @return 收藏量
     */
    Long getCollectCount(Long articleId);

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

    /**
     * 获取文章评论量
     *
     * @param articleId 文章ID
     * @return 评论量
     */
    Long getCommentCount(Long articleId);
}