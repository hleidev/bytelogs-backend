package top.harrylei.forum.service.article.service;

public interface ArticleDetailService {

    /**
     * 插入文章详细
     * 
     * @param articleId 文章ID
     * @param content 文章内容
     * @return 文章详细ID
     */
    Long insertArticleDetail(Long articleId, String content);
}
