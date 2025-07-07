package top.harrylei.forum.service.article.repository.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.forum.api.model.vo.article.req.ArticleQueryParam;
import top.harrylei.forum.api.model.vo.article.vo.ArticleVO;
import top.harrylei.forum.service.article.repository.entity.ArticleDO;
import top.harrylei.forum.service.article.repository.mapper.ArticleMapper;

/**
 * 文章访问对象
 *
 * @author harry
 */
@Repository
public class ArticleDAO extends ServiceImpl<ArticleMapper, ArticleDO> {

    public Long insertArticle(ArticleDO article) {
        this.baseMapper.insert(article);
        return article.getId();
    }

    public void updateDeleted(Long articleId, Integer deleted) {
        getBaseMapper().updateDeleted(articleId, deleted);
    }

    public IPage<ArticleVO> pageArticleVO(ArticleQueryParam queryParam, IPage<ArticleVO> page) {
        // 联表查询，返回包含分类和标签的ArticleVO
        return this.getBaseMapper().pageArticleVO(queryParam, page);
    }

    /**
     * 联表查询完整文章VO（包含分类和标签对象）
     *
     * @param articleId 文章ID
     * @return 完整文章VO
     */
    public ArticleVO getArticleVoByArticleId(Long articleId) {
        return this.getBaseMapper().getArticleVoById(articleId);
    }

    public Integer updateTopping(Long articleId, Integer intValue) {
        return getBaseMapper().updateTopping(articleId, intValue);
    }

    public Integer updateCream(Long articleId, Integer intValue) {
        return getBaseMapper().updateCream(articleId, intValue);
    }

    public Integer updateOfficial(Long articleId, Integer intValue) {
        return getBaseMapper().updateOfficial(articleId, intValue);
    }
}
