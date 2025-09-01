package top.harrylei.community.service.article.repository.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.api.enums.article.CreamStatusEnum;
import top.harrylei.community.api.enums.article.OfficialStatusEnum;
import top.harrylei.community.api.enums.article.ToppingStatusEnum;
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

    public void updateDeleted(Long articleId, DeleteStatusEnum deleted) {
        lambdaUpdate()
                .eq(ArticleDO::getId, articleId)
                .set(ArticleDO::getDeleted, deleted)
                .update();
    }

    public IPage<ArticleVO> pageArticleVO(ArticleQueryParam queryParam, IPage<ArticleVO> page) {
        // 联表查询，返回包含分类和标签的ArticleVO
        return getBaseMapper().pageArticleVO(queryParam, page);
    }

    public boolean updateTopping(Long articleId, ToppingStatusEnum toppingStat) {
        return lambdaUpdate()
                .eq(ArticleDO::getId, articleId)
                .set(ArticleDO::getTopping, toppingStat)
                .update();
    }

    public boolean updateCream(Long articleId, CreamStatusEnum creamStat) {
        return lambdaUpdate()
                .eq(ArticleDO::getId, articleId)
                .set(ArticleDO::getCream, creamStat)
                .update();
    }

    public boolean updateOfficial(Long articleId, OfficialStatusEnum officialStat) {
        return lambdaUpdate()
                .eq(ArticleDO::getId, articleId)
                .set(ArticleDO::getOfficial, officialStat)
                .update();
    }

    public ArticleDO getArticle(Long articleId, DeleteStatusEnum status) {
        return lambdaQuery()
                .eq(ArticleDO::getId, articleId)
                .eq(ArticleDO::getDeleted, status)
                .one();
    }
}
