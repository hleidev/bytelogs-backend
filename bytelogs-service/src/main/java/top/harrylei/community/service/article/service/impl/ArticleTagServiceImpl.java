package top.harrylei.community.service.article.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.service.article.repository.dao.ArticleTagDAO;
import top.harrylei.community.service.article.repository.entity.ArticleTagDO;
import top.harrylei.community.service.article.service.ArticleTagService;

import java.util.ArrayList;
import java.util.List;

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
        // 1. 删除现有的所有标签关联
        deleteByArticleId(articleId);

        // 2. 如果有新标签，批量添加
        if (!CollectionUtils.isEmpty(tagIds)) {
            saveBatch(articleId, tagIds);
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
    private void deleteByArticleId(Long articleId) {
        articleTagDAO.updateDeleted(articleId, DeleteStatusEnum.DELETED);
    }
}
