package top.harrylei.forum.service.article.repository.dao;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import top.harrylei.forum.service.article.repository.entity.ArticleDetailDO;
import top.harrylei.forum.service.article.repository.mapper.ArticleDetailMapper;

/**
 * 文章详细访问对象
 *
 * @author harry
 */
@Repository
public class ArticleDetailDAO extends ServiceImpl<ArticleDetailMapper, ArticleDetailDO> {

    public int updateArticleContent(Long articleId, String content, Integer version) {
        return getBaseMapper().updateArticleContent(articleId, content, version);
    }

    public ArticleDetailDO getLatestContentAndVersionByArticleId(Long articleId) {
        return getBaseMapper().getLatestContentAndVersionByArticleId(articleId);
    }

    public void updateDeleted(Long articleId, Integer deleted) {
        getBaseMapper().updateDeleted(articleId, deleted);
    }

    public ArticleDetailDO getByArticleIdAndVersion(Long articleId, Integer version) {
        return getBaseMapper().getByArticleIdAndVersion(articleId, version);
    }

    public int updateContentByVersion(Long articleId, String content, Integer version) {
        return getBaseMapper().updateContentByVersion(articleId, content, version);
    }
}