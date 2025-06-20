package top.harrylei.forum.service.article.service;

import top.harrylei.forum.api.model.enums.article.PublishStatusEnum;
import top.harrylei.forum.api.model.vo.article.dto.ArticleDTO;
import top.harrylei.forum.api.model.vo.article.vo.ArticleDetailVO;
import top.harrylei.forum.api.model.vo.article.vo.ArticleVO;
import top.harrylei.forum.api.model.vo.page.Page;
import top.harrylei.forum.api.model.vo.page.PageVO;

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
     * 文章详细
     *
     * @param articleId 文章ID
     * @return 文章详细展示对象
     */
    ArticleDetailVO getArticleDetail(Long articleId);

    /**
     * 更新状态
     *
     * @param status 修改状态
     */
    void updateArticleStatus(Long articleId, PublishStatusEnum status);

    /**
     * 分页查询所有文章
     *
     * @param req 分页请求参数
     * @return 分页查询结果
     */
    PageVO<ArticleDTO> page(Page req);

    /**
     * 按用户ID分页查询
     *
     * @param userId 用户ID
     * @param req    分页请求参数
     * @return 分页查询结果
     */
    PageVO<ArticleDTO> page(Long userId, Page req);
}
