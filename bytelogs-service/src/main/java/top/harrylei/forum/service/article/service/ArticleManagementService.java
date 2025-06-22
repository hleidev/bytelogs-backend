package top.harrylei.forum.service.article.service;

import top.harrylei.forum.api.model.enums.article.PublishStatusEnum;

/**
 * 文章管理接口类
 *
 * @author Harry
 */
public interface ArticleManagementService {

    /**
     * 审核文章
     *
     * @param articleId 文章ID
     * @param status    审核状态
     */
    void auditArticle(Long articleId, PublishStatusEnum status);
}
