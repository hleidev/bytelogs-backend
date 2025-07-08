package top.harrylei.forum.service.article.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.service.article.repository.entity.ArticleDetailDO;
import top.harrylei.forum.service.article.repository.mapper.ArticleDetailMapper;

/**
 * 文章详细访问对象
 *
 * @author harry
 */
@Repository
public class ArticleDetailDAO extends ServiceImpl<ArticleDetailMapper, ArticleDetailDO> {

    public void updateDeleted(Long articleId, Integer deleted) {
        getBaseMapper().updateDeleted(articleId, deleted);
    }


    public String getPublishedTitle(Long articleId) {
        return getBaseMapper().getPublishedTitle(articleId);
    }

    /**
     * 获取最新版本（编辑时使用）
     */
    public ArticleDetailDO getLatestVersion(Long articleId) {
        return lambdaQuery()
                .eq(ArticleDetailDO::getArticleId, articleId)
                .eq(ArticleDetailDO::getLatest, YesOrNoEnum.YES.getCode())
                .eq(ArticleDetailDO::getDeleted, YesOrNoEnum.NO.getCode())
                .one();
    }

    /**
     * 获取发布版本（读者查看）
     */
    public ArticleDetailDO getPublishedVersion(Long articleId) {
        return lambdaQuery()
                .eq(ArticleDetailDO::getArticleId, articleId)
                .eq(ArticleDetailDO::getPublished, YesOrNoEnum.YES.getCode())
                .eq(ArticleDetailDO::getDeleted, YesOrNoEnum.NO.getCode())
                .one();
    }

    /**
     * 清除最新版本标记
     */
    public void clearLatestFlag(Long articleId) {
        lambdaUpdate()
                .eq(ArticleDetailDO::getArticleId, articleId)
                .eq(ArticleDetailDO::getLatest, YesOrNoEnum.YES.getCode())
                .eq(ArticleDetailDO::getDeleted, YesOrNoEnum.NO.getCode())
                .set(ArticleDetailDO::getLatest, YesOrNoEnum.NO.getCode())
                .update();
    }

    /**
     * 清除发布版本标记
     */
    public void clearPublishedFlag(Long articleId) {
        lambdaUpdate()
                .eq(ArticleDetailDO::getArticleId, articleId)
                .eq(ArticleDetailDO::getPublished, YesOrNoEnum.YES.getCode())
                .eq(ArticleDetailDO::getDeleted, YesOrNoEnum.NO.getCode())
                .set(ArticleDetailDO::getPublished, YesOrNoEnum.NO.getCode())
                .update();
    }

    /**
     * 获取版本历史列表
     */
    public java.util.List<ArticleDetailDO> getVersionHistory(Long articleId) {
        return lambdaQuery()
                .eq(ArticleDetailDO::getArticleId, articleId)
                .eq(ArticleDetailDO::getDeleted, YesOrNoEnum.NO.getCode())
                .orderByDesc(ArticleDetailDO::getVersion)
                .list();
    }

    /**
     * 根据文章ID和版本号获取特定版本
     *
     * @param articleId 文章ID
     * @param version   版本号
     * @return 文章版本详情
     */
    public ArticleDetailDO getByArticleIdAndVersion(Long articleId, Integer version) {
        return lambdaQuery()
                .eq(ArticleDetailDO::getArticleId, articleId)
                .eq(ArticleDetailDO::getVersion, version)
                .eq(ArticleDetailDO::getDeleted, YesOrNoEnum.NO.getCode())
                .one();
    }
}