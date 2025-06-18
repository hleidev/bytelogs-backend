package top.harrylei.forum.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Select;
import top.harrylei.forum.service.article.repository.entity.ArticleDO;

public interface ArticleMapper extends BaseMapper<ArticleDO> {

    @Select("select user_id from article where id = #{articleId} and deleted = 0")
    Long getUserIdByArticleId(Long articleId);
}
