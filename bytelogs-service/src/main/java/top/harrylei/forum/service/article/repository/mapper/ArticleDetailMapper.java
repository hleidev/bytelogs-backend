package top.harrylei.forum.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.harrylei.forum.service.article.repository.entity.ArticleDetailDO;

public interface ArticleDetailMapper extends BaseMapper<ArticleDetailDO> {

    @Update("update article_detail set content = #{content}, version = version + 1 where article_id = #{articleId} and version = #{version} and deleted = 0")
    int updateArticleContent(Long articleId, String content, Integer version);

    @Select("select content, version from article_detail where article_id = #{articleId} order by version desc limit 1")
    ArticleDetailDO getLatestContentAndVersionByArticleId(Long articleId);

    @Update("update article_detail set deleted = #{deleted} where article_id = #{articleId}")
    void updateDeleted(Long articleId, Integer deleted);

    @Select("select * from article_detail where article_id = #{articleId} and version = #{version} and deleted = 0")
    ArticleDetailDO getByArticleIdAndVersion(Long articleId, Integer version);

    @Update("update article_detail set content = #{content} where article_id = #{articleId} and version = #{version} and deleted = 0")
    int updateContentByVersion(Long articleId, String content, Integer version);
}
