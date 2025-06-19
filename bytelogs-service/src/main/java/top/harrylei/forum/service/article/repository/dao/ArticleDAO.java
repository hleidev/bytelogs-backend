package top.harrylei.forum.service.article.repository.dao;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.service.article.repository.entity.ArticleDO;
import top.harrylei.forum.service.article.repository.mapper.ArticleMapper;

/**
 * 文章访问对象
 */
@Repository
public class ArticleDAO extends ServiceImpl<ArticleMapper, ArticleDO> {

    public Long insertArticle(ArticleDO article) {
        this.baseMapper.insert(article);
        return article.getId();
    }

    public Long getUserIdByArticleId(Long articleId) {
        return this.getBaseMapper().getUserIdByArticleId(articleId);
    }

    public ArticleDO getByArticleId(Long articleId) {
        return lambdaQuery()
                .eq(ArticleDO::getId, articleId)
                .eq(ArticleDO::getDeleted, YesOrNoEnum.NO.getCode())
                .one();
    }

    public void updateDeleted(Long articleId, Integer deleted) {
        getBaseMapper().updateDeleted(articleId, deleted);
    }

    public Long getUserIdByArticleIdIncludeDeleted(Long articleId) {
        return getBaseMapper().getUserIdByArticleIdIncludeDeleted(articleId);
    }

    public int updateStatus(Long articleId, Integer status) {
        return getBaseMapper().updateStatus(articleId, status);
    }
}
