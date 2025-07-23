package top.harrylei.forum.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import top.harrylei.forum.service.article.repository.entity.ArticleDetailDO;

/**
 * 文章详细内容Mapper
 *
 * @author harry
 */
public interface ArticleDetailMapper extends BaseMapper<ArticleDetailDO> {

    @Select("select title from article_detail where article_id = #{articleId} and published = 1 and deleted = 0")
    String getPublishedTitle(Long articleId);
}
