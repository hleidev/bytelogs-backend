package top.harrylei.community.service.article.service;

import java.util.List;

/**
 * 文章标签绑定接口类
 *
 * @author harry
 */
public interface ArticleTagService {

    /**
     * 文章批量绑定标签
     *
     * @param articleId 文章ID
     * @param tagIds    标签ID列表
     */
    void saveBatch(Long articleId, List<Long> tagIds);

    /**
     * 更新文章的标签
     *
     * @param articleId 文章ID
     * @param tagIds    标签ID列表
     */
    void updateTags(Long articleId, List<Long> tagIds);

    /**
     * 通过文章ID查询标签ID列表
     *
     * @param articleId 文章ID
     * @return 标签ID列表
     */
    List<Long> listTagIdsByArticleId(Long articleId);

    /**
     * 通过文章ID列表查询标签ID列表
     *
     * @param articleIds 文章ID列表
     * @return 标签ID列表
     */
    List<Long> listTagIdsByArticleIds(List<Long> articleIds);

    /**
     * 删除绑定
     *
     * @param articleId 文章ID
     */
    void deleteByArticleId(Long articleId);

    /**
     * 恢复绑定
     *
     * @param articleId 文章ID
     */
    void restoreByArticleId(Long articleId);

}
