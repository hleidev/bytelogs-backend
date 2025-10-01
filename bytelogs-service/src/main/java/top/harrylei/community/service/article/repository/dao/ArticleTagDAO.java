package top.harrylei.community.service.article.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.service.article.repository.entity.ArticleTagDO;
import top.harrylei.community.service.article.repository.mapper.ArticleTagMapper;

import java.util.List;

/**
 * 文章标签关系访问对象
 *
 * @author harry
 */
@Repository
public class ArticleTagDAO extends ServiceImpl<ArticleTagMapper, ArticleTagDO> {

    public List<Long> listTagIdsByArticleId(Long articleId) {
        return lambdaQuery()
                .select(ArticleTagDO::getTagId)
                .eq(ArticleTagDO::getArticleId, articleId)
                .eq(ArticleTagDO::getDeleted, DeleteStatusEnum.NOT_DELETED)
                .list()
                .stream()
                .map(ArticleTagDO::getTagId)
                .toList();
    }

    public boolean updateDeleted(Long articleId, DeleteStatusEnum deleted) {
        return lambdaUpdate()
                .eq(ArticleTagDO::getArticleId, articleId)
                .set(ArticleTagDO::getDeleted, deleted)
                .update();
    }

}
