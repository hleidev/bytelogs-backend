package top.harrylei.community.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import top.harrylei.community.api.model.article.req.ArticleQueryParam;
import top.harrylei.community.api.model.article.vo.ArticleVO;
import top.harrylei.community.service.article.repository.entity.ArticleDO;

/**
 * 文章Mapper
 *
 * @author harry
 */
public interface ArticleMapper extends BaseMapper<ArticleDO> {

    ArticleVO getArticleVoById(Long articleId);

    IPage<ArticleVO> pageArticleVO(@Param("query") ArticleQueryParam query, IPage<ArticleVO> page);
}
