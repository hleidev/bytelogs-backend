package top.harrylei.forum.service.article.service.impl;

import java.util.*;
import java.util.stream.Collectors;

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
    public void saveBatch(Long articleId, List<Long> tagIds) {
        List<ArticleTagDO> articleTagList = new ArrayList<>();
        tagIds.forEach(tagId -> {
            ArticleTagDO articleTag = new ArticleTagDO().setArticleId(articleId).setTagId(tagId);
            articleTagList.add(articleTag);
        });
        articleTagDAO.saveBatch(articleTagList);
    }

    /**
     * 更新文章的标签
     *
     * @param articleId 文章ID
     * @param tagIds 标签ID列表
     */
    @Override
    public void updateTags(Long articleId, List<Long> tagIds) {
        List<ArticleTagDO> dbTagIds = articleTagDAO.listIdAndTagIdByArticleId(articleId);

        Map<Long, Long> tagIdToId = dbTagIds.stream()
                .collect(Collectors.toMap(ArticleTagDO::getTagId, ArticleTagDO::getId));

        Set<Long> dbTagIdSet = tagIdToId.keySet();
        if (tagIds.isEmpty()) {
            articleTagDAO.removeBatchByIds(new ArrayList<>(tagIdToId.values()));
            return;
        }

        Set<Long> newTagIdSet = new HashSet<>(tagIds);
        Set<Long> toDeleteIds = dbTagIdSet.stream()
                .filter(id -> !newTagIdSet.contains(id))
                .map(tagIdToId::get)
                .collect(Collectors.toSet());

        Set<Long> toAddTagIds = newTagIdSet.stream()
                .filter(id -> !dbTagIdSet.contains(id))
                .collect(Collectors.toSet());

        if (!toDeleteIds.isEmpty()) {
            articleTagDAO.removeBatchByIds(new ArrayList<>(toDeleteIds));
        }
        if (!toAddTagIds.isEmpty()) {
            saveBatch(articleId, new ArrayList<>(toAddTagIds));
        }
    }

    /**
     * 通过文章ID查询标签ID列表
     * 
     * @param articleId 文章ID
     * @return 标签ID列表
     */
    @Override
    public List<Long> listTagIdsByArticleId(Long articleId) {
        return articleTagDAO.listTagIdsByArticleId(articleId);
    }
}
