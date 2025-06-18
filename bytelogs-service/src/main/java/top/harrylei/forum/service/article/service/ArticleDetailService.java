package top.harrylei.forum.service.article.service;

public interface ArticleDetailService {

    /**
     * 保持文章内容
     * 
     * @param articleId 文章ID
     * @param content 文章内容
     * @return 文章详细ID
     */
    Long saveArticleContent(Long articleId, String content);

    /**
     * 更新文章内容
     *
     * @param articleId 文章ID
     * @param content 文章内容
     */
    void updateArticleContent(Long articleId, String content);

    /**
     * 查询文章内容
     * 
     * @param articleId 文章ID
     * @return 文章内容
     */
    String getContentByArticleId(Long articleId);

    /**
     * 删除文章内容
     * 
     * @param articleId 文章ID
     */
    void deleteByArticleId(Long articleId);

    /**
     * 恢复文章内容
     * @param articleId 文章ID
     */
    void restoreByArticleId(Long articleId);
}
