package top.harrylei.community.service.article.service;

import top.harrylei.community.api.enums.article.ArticlePublishStatusEnum;
import top.harrylei.community.api.enums.article.CreamStatusEnum;
import top.harrylei.community.api.enums.article.OfficialStatusEnum;
import top.harrylei.community.api.enums.article.ToppingStatusEnum;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.api.enums.user.OperateTypeEnum;
import top.harrylei.community.api.model.article.dto.ArticleDTO;

/**
 * 文章命令服务接口
 * 负责文章的保存、更新、删除、状态变更等写操作
 *
 * @author harry
 */
public interface ArticleCommandService {

    /**
     * 保存文章
     *
     * @param articleDTO 文章传输对象
     * @return 文章ID
     */
    Long saveArticle(ArticleDTO articleDTO);

    /**
     * 更新文章
     *
     * @param articleDTO 文章传输对象
     * @return 文章DTO
     */
    ArticleDTO updateArticle(ArticleDTO articleDTO);

    /**
     * 删除文章
     *
     * @param articleId 文章ID
     */
    void deleteArticle(Long articleId);

    /**
     * 恢复文章
     *
     * @param articleId 文章ID
     */
    void restoreArticle(Long articleId);

    /**
     * 发布文章
     *
     * @param articleId 文章ID
     */
    void publishArticle(Long articleId);

    /**
     * 撤销发布
     *
     * @param articleId 文章ID
     */
    void unpublishArticle(Long articleId);

    /**
     * 更新文章状态
     *
     * @param articleId 文章ID
     * @param status    目标状态
     */
    void updateArticleStatus(Long articleId, ArticlePublishStatusEnum status);

    /**
     * 文章操作（点赞/收藏等）
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @param type      操作类型
     */
    void actionArticle(Long userId, Long articleId, OperateTypeEnum type);

    /**
     * 版本回滚
     *
     * @param articleId 文章ID
     * @param version   目标版本号
     * @return 回滚后的文章DTO
     */
    ArticleDTO rollbackToVersion(Long articleId, Integer version);

    /**
     * 更新文章置顶状态
     *
     * @param articleId   文章ID
     * @param toppingStat 置顶状态
     */
    void updateArticleTopping(Long articleId, ToppingStatusEnum toppingStat);

    /**
     * 更新文章加精状态
     *
     * @param articleId 文章ID
     * @param creamStat 加精状态
     */
    void updateArticleCream(Long articleId, CreamStatusEnum creamStat);

    /**
     * 更新文章官方状态
     *
     * @param articleId    文章ID
     * @param officialStat 官方状态
     */
    void updateArticleOfficial(Long articleId, OfficialStatusEnum officialStat);
}