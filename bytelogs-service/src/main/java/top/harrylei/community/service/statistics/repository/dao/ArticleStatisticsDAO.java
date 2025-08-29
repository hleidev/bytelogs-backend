package top.harrylei.community.service.statistics.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.community.service.statistics.repository.entity.ArticleStatisticsDO;
import top.harrylei.community.service.statistics.repository.mapper.ArticleStatisticsMapper;

/**
 * 文章统计数据访问对象
 *
 * @author harry
 */
@Repository
public class ArticleStatisticsDAO extends ServiceImpl<ArticleStatisticsMapper, ArticleStatisticsDO> {

    /**
     * 根据文章ID获取统计记录
     *
     * @param articleId 文章ID
     * @return 统计记录
     */
    public ArticleStatisticsDO getByArticleId(Long articleId) {
        return lambdaQuery()
                .eq(ArticleStatisticsDO::getArticleId, articleId)
                .one();
    }

    /**
     * 增加文章阅读量
     *
     * @param articleId 文章ID
     * @return 是否成功
     */
    public boolean incrementReadCount(Long articleId) {
        ArticleStatisticsDO existingRecord = getByArticleId(articleId);
        if (existingRecord == null) {
            return initArticleStatistics(articleId);
        } else {
            return lambdaUpdate()
                    .eq(ArticleStatisticsDO::getArticleId, articleId)
                    .setSql("read_count = read_count + 1")
                    .update();
        }
    }

    /**
     * 获取文章阅读量
     *
     * @param articleId 文章ID
     * @return 阅读量
     */
    public Long getReadCount(Long articleId) {
        ArticleStatisticsDO statistics = getByArticleId(articleId);
        return statistics != null ? statistics.getReadCount().longValue() : 0L;
    }

    /**
     * 增加文章点赞量
     *
     * @param articleId 文章ID
     * @return 是否成功
     */
    public boolean incrementPraiseCount(Long articleId) {
        ensureStatisticsExists(articleId);
        return lambdaUpdate()
                .eq(ArticleStatisticsDO::getArticleId, articleId)
                .setSql("praise_count = praise_count + 1")
                .update();
    }

    /**
     * 减少文章点赞量
     *
     * @param articleId 文章ID
     * @return 是否成功
     */
    public boolean decrementPraiseCount(Long articleId) {
        return lambdaUpdate()
                .eq(ArticleStatisticsDO::getArticleId, articleId)
                .setSql("praise_count = GREATEST(praise_count - 1, 0)")
                .update();
    }

    /**
     * 获取文章点赞量
     *
     * @param articleId 文章ID
     * @return 点赞量
     */
    public Long getPraiseCount(Long articleId) {
        ArticleStatisticsDO statistics = getByArticleId(articleId);
        return statistics != null ? statistics.getPraiseCount().longValue() : 0L;
    }

    /**
     * 增加文章收藏量
     *
     * @param articleId 文章ID
     * @return 是否成功
     */
    public boolean incrementCollectCount(Long articleId) {
        ensureStatisticsExists(articleId);
        return lambdaUpdate()
                .eq(ArticleStatisticsDO::getArticleId, articleId)
                .setSql("collect_count = collect_count + 1")
                .update();
    }

    /**
     * 减少文章收藏量
     *
     * @param articleId 文章ID
     * @return 是否成功
     */
    public boolean decrementCollectCount(Long articleId) {
        return lambdaUpdate()
                .eq(ArticleStatisticsDO::getArticleId, articleId)
                .setSql("collect_count = GREATEST(collect_count - 1, 0)")
                .update();
    }

    /**
     * 获取文章收藏量
     *
     * @param articleId 文章ID
     * @return 收藏量
     */
    public Long getCollectCount(Long articleId) {
        ArticleStatisticsDO statistics = getByArticleId(articleId);
        return statistics != null ? statistics.getCollectCount().longValue() : 0L;
    }

    /**
     * 增加文章评论量
     *
     * @param articleId 文章ID
     * @return 是否成功
     */
    public boolean incrementCommentCount(Long articleId) {
        ensureStatisticsExists(articleId);
        return lambdaUpdate()
                .eq(ArticleStatisticsDO::getArticleId, articleId)
                .setSql("comment_count = comment_count + 1")
                .update();
    }

    /**
     * 减少文章评论量
     *
     * @param articleId 文章ID
     * @return 是否成功
     */
    public boolean decrementCommentCount(Long articleId) {
        return lambdaUpdate()
                .eq(ArticleStatisticsDO::getArticleId, articleId)
                .setSql("comment_count = GREATEST(comment_count - 1, 0)")
                .update();
    }

    /**
     * 获取文章评论量
     *
     * @param articleId 文章ID
     * @return 评论量
     */
    public Long getCommentCount(Long articleId) {
        ArticleStatisticsDO statistics = getByArticleId(articleId);
        return statistics != null ? statistics.getCommentCount().longValue() : 0L;
    }

    /**
     * 初始化文章统计记录
     *
     * @param articleId 文章ID
     * @return 是否成功
     */
    private boolean initArticleStatistics(Long articleId) {
        ArticleStatisticsDO statistics = new ArticleStatisticsDO()
                .setArticleId(articleId)
                .setReadCount(1)
                .setPraiseCount(0)
                .setCollectCount(0)
                .setCommentCount(0);
        return save(statistics);
    }

    /**
     * 确保统计记录存在
     *
     * @param articleId 文章ID
     */
    private void ensureStatisticsExists(Long articleId) {
        ArticleStatisticsDO existing = getByArticleId(articleId);
        if (existing == null) {
            ArticleStatisticsDO statistics = new ArticleStatisticsDO()
                    .setArticleId(articleId)
                    .setReadCount(0)
                    .setPraiseCount(0)
                    .setCollectCount(0)
                    .setCommentCount(0);
            save(statistics);
        }
    }
}