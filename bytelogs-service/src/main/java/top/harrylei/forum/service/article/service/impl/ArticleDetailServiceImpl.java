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
     * 保存文章详细
     *
     * @param articleId 文章ID
     * @param content   文章内容
     * @return 文章详细ID
     */
    @Override
    public Long saveArticleContent(Long articleId, String content) {
        ArticleDetailDO articleDetail = new ArticleDetailDO()
                .setArticleId(articleId)
                .setContent(content);
        articleDetailDAO.save(articleDetail);
        return articleDetail.getId();
    }

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
     * 查询文章内容
     *
     * @param articleId 文章ID
     * @return 文章内容
     */
    @Override
    public String getContentByArticleId(Long articleId) {
        ArticleDetailDO articleDetailDO = articleDetailDAO.getLatestContentAndVersionByArticleId(articleId);
        return articleDetailDO.getContent();
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
}
