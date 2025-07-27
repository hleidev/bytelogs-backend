package top.harrylei.forum.service.article.service;

import top.harrylei.forum.api.enums.YesOrNoEnum;
import top.harrylei.forum.api.enums.article.ArticleStatusTypeEnum;
import top.harrylei.forum.api.enums.user.OperateTypeEnum;
import top.harrylei.forum.api.enums.article.PublishStatusEnum;
import top.harrylei.forum.api.model.article.dto.ArticleDTO;
import top.harrylei.forum.api.model.article.vo.ArticleVO;

/**
 * 文章命令服务接口
 * 负责文章的保存、更新、删除、状态变更等写操作
 *
 * @author harry
 */
public interface ArticleCommandService {

    /**
     * 保存文章
     *
     * @param articleDTO 文章传输对象
     * @return 文章ID
     */
    Long saveArticle(ArticleDTO articleDTO);

    /**
     * 更新文章
     *
     * @param articleDTO 文章传输对象
     * @return 文章VO
     */
    ArticleVO updateArticle(ArticleDTO articleDTO);

    /**
     * 删除文章
     *
     * @param articleId 文章ID
     */
    void deleteArticle(Long articleId);

    /**
     * 恢复文章
     *
     * @param articleId 文章ID
     */
    void restoreArticle(Long articleId);

    /**
     * 发布文章
     *
     * @param articleId 文章ID
     */
    void publishArticle(Long articleId);

    /**
     * 撤销发布
     *
     * @param articleId 文章ID
     */
    void unpublishArticle(Long articleId);

    /**
     * 更新文章状态
     *
     * @param articleId 文章ID
     * @param status    目标状态
     */
    void updateArticleStatus(Long articleId, PublishStatusEnum status);

    /**
     * 更新文章属性（置顶/加精/官方）
     *
     * @param articleId  文章ID
     * @param statusType 状态类型
     * @param status     是否启用
     */
    void updateArticleProperty(Long articleId, ArticleStatusTypeEnum statusType, YesOrNoEnum status);

    /**
     * 文章操作（点赞/收藏等）
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @param type      操作类型
     */
    void actionArticle(Long userId, Long articleId, OperateTypeEnum type);

    /**
     * 版本回滚
     *
     * @param articleId 文章ID
     * @param version   目标版本号
     * @return 回滚后的文章VO
     */
    ArticleVO rollbackToVersion(Long articleId, Integer version);

    /**
     * 获取文章草稿（用于编辑）
     *
     * @param articleId 文章ID
     * @return 文章VO
     */
    ArticleVO getArticleDraft(Long articleId);
}