package top.harrylei.forum.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import top.harrylei.forum.api.model.vo.article.req.ArticleQueryParam;
import top.harrylei.forum.api.model.vo.article.vo.ArticleVO;
import top.harrylei.forum.service.article.repository.entity.ArticleDO;

public interface ArticleMapper extends BaseMapper<ArticleDO> {

    @Update("update article set deleted = #{deleted} where id = #{articleId}")
    void updateDeleted(Long articleId, Integer deleted);

    @Update("update article set topping = #{value} where id = #{articleId} and deleted = 0")
    Integer updateTopping(Long articleId, Integer value);

    @Update("update article set cream = #{value} where id = #{articleId} and deleted = 0")
    Integer updateCream(Long articleId, Integer value);

    @Update("update article set official = #{value} where id = #{articleId} and deleted = 0")
    Integer updateOfficial(Long articleId, Integer value);

    ArticleVO getArticleVoById(Long articleId);

    IPage<ArticleVO> pageArticleVO(@Param("query") ArticleQueryParam query, IPage<ArticleVO> page);
}
