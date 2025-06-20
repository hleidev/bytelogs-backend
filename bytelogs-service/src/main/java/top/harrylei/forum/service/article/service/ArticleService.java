package top.harrylei.forum.service.article.service;

import top.harrylei.forum.api.model.enums.article.PublishStatusEnum;
import top.harrylei.forum.api.model.vo.article.dto.ArticleDTO;
import top.harrylei.forum.api.model.vo.article.req.ArticleQueryParam;
import top.harrylei.forum.api.model.vo.article.vo.ArticleDetailVO;
import top.harrylei.forum.api.model.vo.article.vo.ArticleVO;
import top.harrylei.forum.api.model.vo.page.PageVO;

/**
 * 文章服务接口类
 *
 * @author Harry
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
     * @param userId    当前用户ID（可为null表示未登录）
     * @param isAdmin   是否为管理员
     * @return 文章详细展示对象
     */
    ArticleDetailVO getArticleDetail(Long articleId, Long userId, boolean isAdmin);

    /**
     * 更新状态
     *
     * @param status 修改状态
     */
    void updateArticleStatus(Long articleId, PublishStatusEnum status);

    /**
     * 分页查询文章（支持多条件查询）
     *
     * @param queryParam 分页查询参数
     * @return 分页查询结果
     */
    PageVO<ArticleVO> pageQuery(ArticleQueryParam queryParam);
}
