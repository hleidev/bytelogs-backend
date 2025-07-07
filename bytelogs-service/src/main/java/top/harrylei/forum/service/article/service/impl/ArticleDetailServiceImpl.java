package top.harrylei.forum.service.article.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.service.article.repository.dao.ArticleDetailDAO;
import top.harrylei.forum.service.article.repository.entity.ArticleDetailDO;
import top.harrylei.forum.service.article.service.ArticleDetailService;

import java.util.List;

/**
 * 文章详细实现类
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleDetailServiceImpl implements ArticleDetailService {

    private final ArticleDetailDAO articleDetailDAO;

    /**
     * 保存文章详细信息
     *
     * @param articleDetailDO 文章详细信息
     * @return 文章详细ID
     */
    @Override
    public Long save(ArticleDetailDO articleDetailDO) {
        articleDetailDAO.save(articleDetailDO);
        return articleDetailDO.getId();
    }

    /**
     * 删除文章内容
     *
     * @param articleId 文章ID
     */
    @Override
    public void deleteByArticleId(Long articleId) {
        articleDetailDAO.updateDeleted(articleId, YesOrNoEnum.YES.getCode());
    }

    /**
     * 恢复文章内容
     *
     * @param articleId 文章ID
     */
    @Override
    public void restoreByArticleId(Long articleId) {
        articleDetailDAO.updateDeleted(articleId, YesOrNoEnum.NO.getCode());
    }

    @Override
    public String getPublishedTitle(Long articleId) {
        return articleDetailDAO.getPublishedTitle(articleId);
    }

    @Override
    public ArticleDetailDO getLatestVersion(Long articleId) {
        return articleDetailDAO.getLatestVersion(articleId);
    }

    @Override
    public ArticleDetailDO getPublishedVersion(Long articleId) {
        return articleDetailDAO.getPublishedVersion(articleId);
    }

    @Override
    public void clearLatestFlag(Long articleId) {
        articleDetailDAO.clearLatestFlag(articleId);
    }

    @Override
    public void clearPublishedFlag(Long articleId) {
        articleDetailDAO.clearPublishedFlag(articleId);
    }

    @Override
    public List<ArticleDetailDO> getVersionHistory(Long articleId) {
        return articleDetailDAO.getVersionHistory(articleId);
    }

    @Override
    public boolean updateById(ArticleDetailDO articleDetailDO) {
        return articleDetailDAO.updateById(articleDetailDO);
    }
}
