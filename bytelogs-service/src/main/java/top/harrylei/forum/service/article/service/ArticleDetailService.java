package top.harrylei.forum.service.article.service;

public interface ArticleDetailService {

    /**
     * 更新文章内容
     *
     * @param articleId 文章ID
     * @param content   文章内容
     */
    void updateArticleContent(Long articleId, String content);

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
     * 保存文章内容
     *
     * @param articleId 文章ID
     * @param content   文章内容
     * @param version   版本号
     * @return 文章详细ID
     */
    Long saveArticleContent(Long articleId, String content, Integer version);

    /**
     * 根据版本获取文章内容
     *
     * @param articleId 文章ID
     * @param version   版本号
     * @return 文章内容
     */
    String getContentByVersion(Long articleId, Integer version);

    /**
     * 更新指定版本的文章内容
     *
     * @param articleId 文章ID
     * @param content   文章内容
     * @param version   版本号
     */
    void updateContentByVersion(Long articleId, String content, Integer version);
}
