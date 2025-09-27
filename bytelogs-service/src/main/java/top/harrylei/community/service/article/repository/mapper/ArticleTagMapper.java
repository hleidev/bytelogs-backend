package top.harrylei.community.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.harrylei.community.service.article.repository.entity.ArticleTagDO;

import java.util.List;

/**
 * 文章标签关系Mapper
 *
 * @author harry
 */
public interface ArticleTagMapper extends BaseMapper<ArticleTagDO> {

    @Select("select tag_id from article_tag where article_id = #{articleId} and deleted = 0")
    List<Long> getTagIdsByArticleId(Long articleId);

    @Select("<script>" +
            "select tag_id from article_tag where deleted = 0 and article_id in " +
            "<foreach collection='articleIds' item='articleId' open='(' separator=',' close=')'>" +
            "#{articleId}" +
            "</foreach>" +
            "</script>")
    List<Long> getTagIdsByArticleIds(@Param("articleIds") List<Long> articleIds);

    @Select("select id, tag_id from article_tag where article_id = #{articleId} and deleted = 0")
    List<ArticleTagDO> listIdAndTagIdByArticleId(Long articleId);

}
