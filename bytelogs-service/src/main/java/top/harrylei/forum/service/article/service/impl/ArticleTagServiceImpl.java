package top.harrylei.forum.service.article.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.service.article.repository.dao.ArticleTagDAO;
import top.harrylei.forum.service.article.repository.entity.ArticleTagDO;
import top.harrylei.forum.service.article.service.ArticleTagService;

/**
 * 文章标签绑定实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleTagServiceImpl implements ArticleTagService {

    private final ArticleTagDAO articleTagDAO;

    /**
     * 文章批量绑定标签
     *
     * @param articleId 文章ID
     * @param tagIds 标签ID列表
     */
    @Override
    public void batchBindTagsToArticle(Long articleId, List<Long> tagIds) {
        List<ArticleTagDO> articleTagList = new ArrayList<>();
        tagIds.forEach(tagId -> {
            ArticleTagDO articleTag = new ArticleTagDO()
                    .setArticleId(articleId)
                    .setTagId(tagId);
            articleTagList.add(articleTag);
        });
        articleTagDAO.saveBatch(articleTagList);
    }
}
