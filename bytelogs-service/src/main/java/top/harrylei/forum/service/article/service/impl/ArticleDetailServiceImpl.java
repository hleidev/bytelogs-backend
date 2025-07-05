package top.harrylei.forum.service.article.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.harrylei.forum.api.model.enums.ErrorCodeEnum;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.service.article.repository.dao.ArticleDetailDAO;
import top.harrylei.forum.service.article.repository.entity.ArticleDetailDO;
import top.harrylei.forum.service.article.service.ArticleDetailService;

import java.util.Objects;

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
     * 更新文章内容
     *
     * @param articleId 文章ID
     * @param content   文章内容
     */
    @Override
    public void updateArticleContent(Long articleId, String content) {
        ArticleDetailDO articleDetail = articleDetailDAO.getLatestContentAndVersionByArticleId(articleId);
        if (!Objects.equals(content, articleDetail.getContent())) {
            int updateCount = articleDetailDAO.updateArticleContent(articleId, content, articleDetail.getVersion());

            if (updateCount == 0) {
                log.warn("文章内容更新失败，版本冲突：articleId={}, version={}", articleId, articleDetail.getVersion());
                ExceptionUtil.error(ErrorCodeEnum.ARTICLE_VERSION_CONFLICT);
            }

            log.debug("文章内容更新成功：articleId={}, version={} -> {}",
                      articleId, articleDetail.getVersion(), articleDetail.getVersion() + 1);
        }
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

    /**
     * 保存文章内容
     *
     * @param articleId 文章ID
     * @param content   文章内容
     * @param version   版本号
     * @return 文章详细ID
     */
    @Override
    public Long saveArticleContent(Long articleId, String content, Integer version) {
        ArticleDetailDO articleDetail = new ArticleDetailDO()
                .setArticleId(articleId)
                .setContent(content)
                .setVersion(version);
        articleDetailDAO.save(articleDetail);
        return articleDetail.getId();
    }

    /**
     * 根据版本获取文章内容
     *
     * @param articleId 文章ID
     * @param version   版本号
     * @return 文章内容
     */
    @Override
    public String getContentByVersion(Long articleId, Integer version) {
        ArticleDetailDO detail = articleDetailDAO.getByArticleIdAndVersion(articleId, version);
        return detail != null ? detail.getContent() : "";
    }

    /**
     * 更新指定版本的文章内容
     *
     * @param articleId 文章ID
     * @param content   文章内容
     * @param version   版本号
     */
    @Override
    public void updateContentByVersion(Long articleId, String content, Integer version) {
        int updateCount = articleDetailDAO.updateContentByVersion(articleId, content, version);
        ExceptionUtil.errorIf(updateCount == 0, ErrorCodeEnum.ARTICLE_NOT_EXISTS, 
                "更新文章内容失败: articleId=" + articleId + ", version=" + version);
    }
}
