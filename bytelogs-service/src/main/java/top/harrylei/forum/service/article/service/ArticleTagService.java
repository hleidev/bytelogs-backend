package top.harrylei.forum.service.article.service;

import java.util.List;

/**
 * 文章标签绑定接口类
 */
public interface ArticleTagService {

    /**
     * 文章批量绑定标签
     * 
     * @param articleId 文章ID
     * @param tagIds 标签ID列表
     */
    void batchBindTagsToArticle(Long articleId, List<Long> tagIds);
}
