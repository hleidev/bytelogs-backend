package top.harrylei.community.service.article.repository.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.community.api.enums.YesOrNoEnum;
import top.harrylei.community.api.model.article.req.ArticleQueryParam;
import top.harrylei.community.api.model.article.vo.ArticleVO;
import top.harrylei.community.service.article.repository.entity.ArticleDO;
import top.harrylei.community.service.article.repository.mapper.ArticleMapper;

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
        lambdaUpdate()
                .eq(ArticleDO::getId, articleId)
                .set(ArticleDO::getDeleted, deleted)
                .update();
    }

    public IPage<ArticleVO> pageArticleVO(ArticleQueryParam queryParam, IPage<ArticleVO> page) {
        // 联表查询，返回包含分类和标签的ArticleVO
        return getBaseMapper().pageArticleVO(queryParam, page);
    }

    public boolean updateTopping(Long articleId, Integer intValue) {
        return lambdaUpdate()
                .eq(ArticleDO::getId, articleId)
                .eq(ArticleDO::getDeleted, YesOrNoEnum.NO.getCode())
                .set(ArticleDO::getTopping, intValue)
                .update();
    }

    public boolean updateCream(Long articleId, Integer intValue) {
        return lambdaUpdate()
                .eq(ArticleDO::getId, articleId)
                .eq(ArticleDO::getDeleted, YesOrNoEnum.NO.getCode())
                .set(ArticleDO::getCream, intValue)
                .update();
    }

    public boolean updateOfficial(Long articleId, Integer intValue) {
        return lambdaUpdate()
                .eq(ArticleDO::getId, articleId)
                .eq(ArticleDO::getDeleted, YesOrNoEnum.NO.getCode())
                .set(ArticleDO::getOfficial, intValue)
                .update();
    }
}
