package top.harrylei.forum.service.article.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.article.PublishStatusEnum;
import top.harrylei.forum.api.model.vo.article.dto.ArticleDTO;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.util.NumUtil;
import top.harrylei.forum.service.article.converted.ArticleStructMapper;
import top.harrylei.forum.service.article.repository.dao.ArticleDAO;
import top.harrylei.forum.service.article.repository.entity.ArticleDO;
import top.harrylei.forum.service.article.service.ArticleDetailService;
import top.harrylei.forum.service.article.service.ArticleService;
import top.harrylei.forum.service.article.service.ArticleTagService;

/**
 * 文章服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final TransactionTemplate transactionTemplate;
    private final ArticleDAO articleDAO;
    private final ArticleStructMapper articleStructMapper;
    private final ArticleDetailService articleDetailService;
    private final ArticleTagService articleTagService;

    /**
     * 保存文章
     *
     * @param articleDTO 文章传输对象
     * @return 文章ID
     */
    @Override
    public Long saveArticle(ArticleDTO articleDTO) {
        ArticleDO article = articleStructMapper.toDO(articleDTO);
        return transactionTemplate.execute(status -> {
            Long articleId;
            if (NumUtil.nullOrZero(article.getId())) {
                articleId = insertArticle(article, articleDTO.getContent(), articleDTO.getTagIds());
                log.info("新建文章成功 title={}", article.getTitle());
            } else {
                articleId = updateArticle(article);
                log.info("更新文章成功 title={}", article.getTitle());
            }
            return articleId;
        });
    }

    private Long insertArticle(ArticleDO article, String content, List<Long> tagIds) {
        if (needToReview(article)) {
            article.setStatus(PublishStatusEnum.REVIEW.getCode());
        }
        Long articleId = articleDAO.insertArticle(article);
        articleDetailService.insertArticleDetail(articleId, content);

        if (tagIds != null && !tagIds.isEmpty()) {
            articleTagService.batchBindTagsToArticle(articleId, tagIds);
        }

        return articleId;
    }

    private boolean needToReview(ArticleDO article) {
        if (ReqInfoContext.getContext().isAdmin()) {
            return false;
        }
        // TODO 添加用户白名单
        return Objects.equals(article.getStatus(), PublishStatusEnum.PUBLISHED.getCode());
    }

    private Long updateArticle(ArticleDO article) {
        // TODO 文章更新
        return 0L;
    }
}
