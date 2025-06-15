package top.harrylei.forum.service.article.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.harrylei.forum.service.article.repository.dao.ArticleDetailDAO;
import top.harrylei.forum.service.article.repository.entity.ArticleDetailDO;
import top.harrylei.forum.service.article.service.ArticleDetailService;

/**
 * 文章详细实现类
 */
@Service
@RequiredArgsConstructor
public class ArticleDetailServiceImpl implements ArticleDetailService {

    private final ArticleDetailDAO articleDetailDAO;

    /**
     * 插入文章详细
     *
     * @param articleId 文章ID
     * @param content 文章内容
     * @return 文章详细ID
     */
    @Override
    public Long insertArticleDetail(Long articleId, String content) {
        ArticleDetailDO articleDetail = new ArticleDetailDO()
                .setArticleId(articleId)
                .setContent(content);
        articleDetailDAO.save(articleDetail);
        return articleDetail.getId();
    }
}
