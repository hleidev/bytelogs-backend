package top.harrylei.forum.service.article.repository.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import top.harrylei.forum.service.article.repository.entity.ArticleTagDO;

public interface ArticleTagMapper extends BaseMapper<ArticleTagDO> {

    @Select("select tag_id from article_tag where article_id = #{articleId} and deleted = 0")
    List<Long> getTagIdsByArticleId(Long articleId);

    @Select("select id, tag_id from article_tag where article_id = #{articleId} and deleted = 0")
    List<ArticleTagDO> listIdAndTagIdByArticleId(Long articleId);
}
