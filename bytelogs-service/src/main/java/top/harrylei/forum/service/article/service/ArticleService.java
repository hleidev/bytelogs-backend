package top.harrylei.forum.service.article.service;

import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.enums.article.ArticleStatusTypeEnum;
import top.harrylei.forum.api.model.enums.OperateTypeEnum;
import top.harrylei.forum.api.model.enums.article.PublishStatusEnum;
import top.harrylei.forum.api.model.vo.article.dto.ArticleDTO;
import top.harrylei.forum.api.model.vo.article.req.ArticleQueryParam;
import top.harrylei.forum.api.model.vo.article.vo.ArticleDetailVO;
import top.harrylei.forum.api.model.vo.article.vo.ArticleVO;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.service.article.repository.entity.ArticleDO;

/**
 * 文章服务接口类
 *
 * @author harry
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
     * 文章详细（支持可选登录）
     *
     * @param articleId 文章ID
     * @return 文章详细展示对象
     */
    ArticleDetailVO getArticleDetail(Long articleId);

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
     * 分页查询文章（支持多条件查询）
     *
     * @param queryParam 分页查询参数
     * @return 分页查询结果
     */
    PageVO<ArticleVO> pageQuery(ArticleQueryParam queryParam);

    /**
     * 更新文章属性标识（置顶/加精/官方）
     *
     * @param articleId  文章ID
     * @param statusType 状态类型
     * @param status     是否启用
     */
    void updateArticleProperty(Long articleId, ArticleStatusTypeEnum statusType, YesOrNoEnum status);

    /**
     * 统一的文章获取方法
     *
     * @param articleId 文章ID
     * @return 文章DO对象
     */
    ArticleDO getArticleById(Long articleId);

    /**
     * 文章操作
     *
     * @param articleId 文章ID
     * @param type      操作类型
     */
    void actionArticle(Long articleId, OperateTypeEnum type);

    /**
     * 回滚到指定版本
     *
     * @param articleId 文章ID
     * @param version   目标版本号
     * @return 回滚后的文章VO
     */
    ArticleVO rollbackToVersion(Long articleId, Integer version);
}
