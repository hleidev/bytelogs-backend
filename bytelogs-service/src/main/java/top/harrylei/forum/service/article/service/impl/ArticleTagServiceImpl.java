package top.harrylei.forum.service.article.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.harrylei.forum.api.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.article.vo.TagSimpleVO;
import top.harrylei.forum.service.article.repository.dao.ArticleTagDAO;
import top.harrylei.forum.service.article.repository.entity.ArticleTagDO;
import top.harrylei.forum.service.article.service.ArticleTagService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 文章标签绑定实现类
 *
 * @author harry
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
     * @param tagIds    标签ID列表
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
     * @param tagIds    标签ID列表
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

    /**
     * 删除绑定
     *
     * @param articleId 文章ID
     */
    @Override
    public void deleteByArticleId(Long articleId) {
        articleTagDAO.updateDeleted(articleId, YesOrNoEnum.YES.getCode());
    }

    /**
     * 恢复绑定
     *
     * @param articleId 文章ID
     */
    @Override
    public void restoreByArticleId(Long articleId) {
        articleTagDAO.updateDeleted(articleId, YesOrNoEnum.NO.getCode());
    }

    /**
     * 通过文章ID列表查询标签简单展示对象
     *
     * @param articleIds 文章ID列表
     * @return 标签简单展示对象列表
     */
    @Override
    public List<TagSimpleVO> listTagSimpleVoByArticleIds(List<Long> articleIds) {
        if (CollectionUtils.isEmpty(articleIds)) {
            return List.of();
        }

        return articleTagDAO.listTagSimpleVoByArticleIds(articleIds);
    }
}
