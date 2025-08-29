package top.harrylei.community.service.article.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.service.article.repository.entity.ArticleDetailDO;
import top.harrylei.community.service.article.repository.mapper.ArticleDetailMapper;

import java.util.List;

/**
 * 文章详细访问对象
 *
 * @author harry
 */
@Repository
public class ArticleDetailDAO extends ServiceImpl<ArticleDetailMapper, ArticleDetailDO> {

    public void updateDeleted(Long articleId, DeleteStatusEnum deleted) {
        lambdaUpdate()
                .eq(ArticleDetailDO::getArticleId, articleId)
                .set(ArticleDetailDO::getDeleted, deleted)
                .update();
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
                .eq(ArticleDetailDO::getLatest, DeleteStatusEnum.DELETED)
                .eq(ArticleDetailDO::getDeleted, DeleteStatusEnum.NOT_DELETED)
                .one();
    }

    /**
     * 获取发布版本（读者查看）
     */
    public ArticleDetailDO getPublishedVersion(Long articleId) {
        return lambdaQuery()
                .eq(ArticleDetailDO::getArticleId, articleId)
                .eq(ArticleDetailDO::getPublished, DeleteStatusEnum.DELETED)
                .eq(ArticleDetailDO::getDeleted, DeleteStatusEnum.NOT_DELETED)
                .one();
    }

    /**
     * 清除最新版本标记
     */
    public void clearLatestFlag(Long articleId) {
        lambdaUpdate()
                .eq(ArticleDetailDO::getArticleId, articleId)
                .eq(ArticleDetailDO::getLatest, DeleteStatusEnum.DELETED)
                .eq(ArticleDetailDO::getDeleted, DeleteStatusEnum.NOT_DELETED)
                .set(ArticleDetailDO::getLatest, DeleteStatusEnum.NOT_DELETED)
                .update();
    }

    /**
     * 清除发布版本标记
     */
    public void clearPublishedFlag(Long articleId) {
        lambdaUpdate()
                .eq(ArticleDetailDO::getArticleId, articleId)
                .eq(ArticleDetailDO::getPublished, DeleteStatusEnum.DELETED)
                .eq(ArticleDetailDO::getDeleted, DeleteStatusEnum.NOT_DELETED)
                .set(ArticleDetailDO::getPublished, DeleteStatusEnum.NOT_DELETED)
                .update();
    }

    /**
     * 获取版本历史列表
     */
    public List<ArticleDetailDO> getVersionHistory(Long articleId) {
        return lambdaQuery()
                .eq(ArticleDetailDO::getArticleId, articleId)
                .eq(ArticleDetailDO::getDeleted, DeleteStatusEnum.NOT_DELETED)
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
                .eq(ArticleDetailDO::getDeleted, DeleteStatusEnum.NOT_DELETED)
                .one();
    }
}