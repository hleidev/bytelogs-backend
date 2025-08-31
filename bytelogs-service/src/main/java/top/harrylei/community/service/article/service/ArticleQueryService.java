package top.harrylei.community.service.article.service;

import top.harrylei.community.api.model.article.req.ArticleQueryParam;
import top.harrylei.community.api.model.article.vo.ArticleVO;
import top.harrylei.community.api.model.article.dto.ArticleDTO;
import top.harrylei.community.api.model.page.PageVO;
import top.harrylei.community.service.article.repository.entity.ArticleDO;

/**
 * 文章查询服务接口
 * 负责文章的各种查询操作
 *
 * @author harry
 */
public interface ArticleQueryService {

    /**
     * 分页查询文章
     *
     * @param queryParam 查询参数
     * @return 分页查询结果
     */
    PageVO<ArticleVO> pageQuery(ArticleQueryParam queryParam);

    /**
     * 构建基础ArticleDTO
     *
     * @param articleId       文章ID
     * @param isLatestVersion 是否使用最新版本
     * @return 文章DTO
     */
    ArticleDTO getArticle(Long articleId, boolean isLatestVersion);
}