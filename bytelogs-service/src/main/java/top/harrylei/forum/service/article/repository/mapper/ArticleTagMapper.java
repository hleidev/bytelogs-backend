package top.harrylei.forum.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.harrylei.forum.api.model.vo.article.vo.TagSimpleVO;
import top.harrylei.forum.service.article.repository.entity.ArticleTagDO;

import java.util.List;

public interface ArticleTagMapper extends BaseMapper<ArticleTagDO> {

    @Select("select tag_id from article_tag where article_id = #{articleId} and deleted = 0")
    List<Long> getTagIdsByArticleId(Long articleId);

    @Select("select id, tag_id from article_tag where article_id = #{articleId} and deleted = 0")
    List<ArticleTagDO> listIdAndTagIdByArticleId(Long articleId);

    @Update("update article_tag set deleted = #{deleted} where article_id = #{articleId}")
    void updateDeleted(Long articleId, Integer deleted);

    List<TagSimpleVO> listTagSimpleVoByArticleIds(@Param("articleIds") List<Long> articleIds);
}
