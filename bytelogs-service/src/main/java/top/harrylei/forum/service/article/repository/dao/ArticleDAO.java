package top.harrylei.forum.service.article.repository.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.vo.article.req.ArticleQueryParam;
import top.harrylei.forum.api.model.vo.article.vo.ArticleVO;
import top.harrylei.forum.service.article.repository.entity.ArticleDO;
import top.harrylei.forum.service.article.repository.mapper.ArticleMapper;

/**
 * 文章访问对象
 *
 * @author Harry
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

    public IPage<ArticleDO> pageArticle(ArticleQueryParam queryParam, IPage<ArticleDO> page) {
        return lambdaQuery()
                // 基础过滤条件
                .like(queryParam.getTitle() != null, ArticleDO::getTitle, queryParam.getTitle())
                .eq(queryParam.getUserId() != null, ArticleDO::getUserId, queryParam.getUserId())
                .eq(queryParam.getCategoryId() != null, ArticleDO::getCategoryId, queryParam.getCategoryId())
                .eq(queryParam.getStatus() != null, ArticleDO::getStatus,
                    queryParam.getStatus() != null ? queryParam.getStatus().getCode() : null)
                // 删除状态过滤（默认只查询未删除的）
                .eq(ArticleDO::getDeleted,
                    queryParam.getDeleted() != null ? queryParam.getDeleted() : YesOrNoEnum.NO.getCode())
                // 时间范围过滤
                .ge(queryParam.getCreateTimeStart() != null, ArticleDO::getCreateTime, queryParam.getCreateTimeStart())
                .le(queryParam.getCreateTimeEnd() != null, ArticleDO::getCreateTime, queryParam.getCreateTimeEnd())
                // 排序
                .orderByDesc(ArticleDO::getCreateTime)
                .page(page);
    }

    /**
     * 联表查询完整文章VO（包含分类和标签对象）
     *
     * @param articleId 文章ID
     * @return 完整文章VO
     */
    public ArticleVO getArticleVoByArticleId(Long articleId) {
        return this.getBaseMapper().getArticleVOById(articleId);
    }
}
