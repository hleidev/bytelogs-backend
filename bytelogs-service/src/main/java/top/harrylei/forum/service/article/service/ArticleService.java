package top.harrylei.forum.service.article.service;

import top.harrylei.forum.api.model.vo.article.dto.ArticleDTO;

/**
 * 文章服务接口类
 */
public interface ArticleService {

    /**
     * 保存文章
     *
     * @param articleDTO 文章传输DTO
     * @return 文章ID
     */
    Long saveArticle(ArticleDTO articleDTO);
}
