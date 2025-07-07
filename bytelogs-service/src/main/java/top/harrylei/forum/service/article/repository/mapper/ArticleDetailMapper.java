package top.harrylei.forum.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.harrylei.forum.service.article.repository.entity.ArticleDetailDO;

public interface ArticleDetailMapper extends BaseMapper<ArticleDetailDO> {

    @Update("update article_detail set deleted = #{deleted} where article_id = #{articleId}")
    void updateDeleted(Long articleId, Integer deleted);

    @Select("select title from article_detail where article_id = #{articleId} and published = 1 and deleted = 0")
    String getPublishedTitle(Long articleId);
}
