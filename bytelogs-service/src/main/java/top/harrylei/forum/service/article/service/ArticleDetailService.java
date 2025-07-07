package top.harrylei.forum.service.article.service;

import top.harrylei.forum.service.article.repository.entity.ArticleDetailDO;

import java.util.List;

/**
 * 文章详细服务层
 *
 * @author harry
 */
public interface ArticleDetailService {

    /**
     * 保存文章详细信息
     *
     * @param articleDetailDO 文章详细信息
     * @return 文章详细ID
     */
    Long save(ArticleDetailDO articleDetailDO);

    /**
     * 更新文章详细信息
     *
     * @param articleDetailDO 文章详细信息
     * @return 是否更新成功
     */
    boolean updateById(ArticleDetailDO articleDetailDO);

    /**
     * 删除文章内容
     *
     * @param articleId 文章ID
     */
    void deleteByArticleId(Long articleId);

    /**
     * 恢复文章内容
     *
     * @param articleId 文章ID
     */
    void restoreByArticleId(Long articleId);

    /**
     * 获取文章已发布版本的标题
     *
     * @param articleId 文章ID
     * @return 文章标题，如果没有已发布版本则返回 null
     */
    String getPublishedTitle(Long articleId);

    /**
     * 获取最新版本（编辑时使用）
     *
     * @param articleId 文章ID
     * @return 最新版本详情
     */
    ArticleDetailDO getLatestVersion(Long articleId);

    /**
     * 获取发布版本（读者查看）
     *
     * @param articleId 文章ID
     * @return 发布版本详情
     */
    ArticleDetailDO getPublishedVersion(Long articleId);

    /**
     * 清除最新版本标记
     *
     * @param articleId 文章ID
     */
    void clearLatestFlag(Long articleId);

    /**
     * 清除发布版本标记
     *
     * @param articleId 文章ID
     */
    void clearPublishedFlag(Long articleId);

    /**
     * 获取版本历史列表
     *
     * @param articleId 文章ID
     * @return 版本历史列表
     */
    List<ArticleDetailDO> getVersionHistory(Long articleId);
}
