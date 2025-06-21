package top.harrylei.forum.service.article.service;

import top.harrylei.forum.api.model.vo.article.vo.TagSimpleVO;

import java.util.List;

/**
 * 文章标签绑定接口类
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

    /**
     * 通过文章ID列表插叙标签简单展示对象
     *
     * @param articleIds 文章ID列表
     * @return 标签简单展示对象
     */
    List<TagSimpleVO> listTagSimpleVoByArticleIds(List<Long> articleIds);
}
