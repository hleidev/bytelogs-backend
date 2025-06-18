package top.harrylei.forum.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.harrylei.forum.service.article.repository.entity.ArticleDO;

public interface ArticleMapper extends BaseMapper<ArticleDO> {

    @Select("select user_id from article where id = #{articleId} and deleted = 0")
    Long getUserIdByArticleId(Long articleId);

    @Update("update article set deleted = #{deleted} where id = #{articleId}")
    void updateDeleted(Long articleId, Integer deleted);

    @Select("select user_id from article where id = #{articleId}")
    Long getUserIdByArticleIdIncludeDeleted(Long articleId);
}
