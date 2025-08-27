package top.harrylei.forum.service.article.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.forum.api.model.article.vo.TagSimpleVO;
import top.harrylei.forum.service.article.repository.entity.ArticleTagDO;
import top.harrylei.forum.service.article.repository.mapper.ArticleTagMapper;

import java.util.List;

/**
 * 文章标签关系访问对象
 *
 * @author harry
 */
@Repository
public class ArticleTagDAO extends ServiceImpl<ArticleTagMapper, ArticleTagDO> {

    public List<Long> listTagIdsByArticleId(Long articleId) {
        return getBaseMapper().getTagIdsByArticleId(articleId);
    }

    public List<ArticleTagDO> listIdAndTagIdByArticleId(Long articleId) {
        return getBaseMapper().listIdAndTagIdByArticleId(articleId);
    }

    public void updateDeleted(Long articleId, Integer deleted) {
        getBaseMapper().updateDeleted(articleId, deleted);
    }

    public List<TagSimpleVO> listTagSimpleVoByArticleIds(List<Long> articleIds) {
        return getBaseMapper().listTagSimpleVoByArticleIds(articleIds);
    }
}
