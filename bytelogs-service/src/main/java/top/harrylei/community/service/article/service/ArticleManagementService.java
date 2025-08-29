package top.harrylei.community.service.article.service;

import top.harrylei.community.api.enums.article.ArticlePublishStatusEnum;
import top.harrylei.community.api.enums.article.CreamStatusEnum;
import top.harrylei.community.api.enums.article.OfficialStatusEnum;
import top.harrylei.community.api.enums.article.ToppingStatusEnum;

import java.util.List;

/**
 * 文章管理接口类
 *
 * @author harry
 */
public interface ArticleManagementService {

    /**
     * 审核文章（支持单个和批量）
     *
     * @param articleIds 文章ID列表（单个文章传单元素列表）
     * @param status     审核状态
     */
    void auditArticles(List<Long> articleIds, ArticlePublishStatusEnum status);

    /**
     * 删除文章（支持单个和批量）
     *
     * @param articleIds 文章ID列表（单个文章传单元素列表）
     */
    void deleteArticles(List<Long> articleIds);

    /**
     * 恢复文章（支持单个和批量）
     *
     * @param articleIds 文章ID列表（单个文章传单元素列表）
     */
    void restoreArticles(List<Long> articleIds);

    /**
     * 批量更新文章置顶状态
     *
     * @param articleIds  文章ID列表
     * @param toppingStat 置顶状态
     */
    void updateArticleTopping(List<Long> articleIds, ToppingStatusEnum toppingStat);

    /**
     * 批量更新文章加精状态
     *
     * @param articleIds 文章ID列表
     * @param creamStat  加精状态
     */
    void updateArticleCream(List<Long> articleIds, CreamStatusEnum creamStat);

    /**
     * 批量更新文章官方状态
     *
     * @param articleIds 文章ID列表
     * @param officialStat  官方状态
     */
    void updateArticleOfficial(List<Long> articleIds, OfficialStatusEnum officialStat);
}
