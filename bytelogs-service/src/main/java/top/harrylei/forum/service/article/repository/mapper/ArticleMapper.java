package top.harrylei.forum.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import top.harrylei.forum.api.model.article.req.ArticleQueryParam;
import top.harrylei.forum.api.model.article.vo.ArticleVO;
import top.harrylei.forum.service.article.repository.entity.ArticleDO;

/**
 * 文章Mapper
 *
 * @author harry
 */
public interface ArticleMapper extends BaseMapper<ArticleDO> {

    ArticleVO getArticleVoById(Long articleId);

    IPage<ArticleVO> pageArticleVO(@Param("query") ArticleQueryParam query, IPage<ArticleVO> page);
}
