package top.harrylei.forum.service.article.service;

import top.harrylei.forum.api.model.vo.article.dto.ArticleDTO;
import top.harrylei.forum.api.model.vo.article.vo.ArticleVO;

/**
 * 文章服务接口类
 */
public interface ArticleService {

    /**
     * 保存文章
     *
     * @param articleDTO 文章传输对象
     * @return 文章ID
     */
    Long saveArticle(ArticleDTO articleDTO);

    /**
     * 编辑文章
     *
     * @param articleDTO 文章传输对象
     * @param editor 编辑用户ID
     * @return 文章VO
     */
    ArticleVO updateArticle(ArticleDTO articleDTO, Long editor);

    /**
     * 删除文章
     *
     * @param articleId 文章ID
     * @param operatorId 操作者ID
     */
    void deleteArticle(Long articleId, Long operatorId);

    /**
     * 恢复文章
     *
     * @param articleId 文章ID
     * @param operatorId 操作者ID
     */
    void restoreArticle(Long articleId, Long operatorId);
}
