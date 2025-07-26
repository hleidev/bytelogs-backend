package top.harrylei.forum.service.article.service;

import top.harrylei.forum.api.model.article.req.ArticleQueryParam;
import top.harrylei.forum.api.model.article.vo.ArticleDetailVO;
import top.harrylei.forum.api.model.article.vo.ArticleVO;
import top.harrylei.forum.api.model.page.PageVO;
import top.harrylei.forum.service.article.repository.entity.ArticleDO;

/**
 * 文章查询服务接口
 * 负责文章的各种查询操作
 *
 * @author harry
 */
public interface ArticleQueryService {

    /**
     * 获取文章详情
     *
     * @param articleId 文章ID
     * @return 文章详情VO
     */
    ArticleDetailVO getArticleDetail(Long articleId);

    /**
     * 分页查询文章
     *
     * @param queryParam 查询参数
     * @return 分页查询结果
     */
    PageVO<ArticleVO> pageQuery(ArticleQueryParam queryParam);

    /**
     * 获取文章基础信息
     *
     * @param articleId 文章ID
     * @return 文章DO对象
     */
    ArticleDO getArticleById(Long articleId);

    /**
     * 构建基础ArticleVO
     *
     * @param articleId        文章ID
     * @param useLatestVersion 是否使用最新版本
     * @return 文章VO
     */
    ArticleVO getArticleVO(Long articleId, boolean useLatestVersion);
}