package top.harrylei.forum.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.harrylei.forum.api.model.vo.article.req.ArticleQueryParam;
import top.harrylei.forum.api.model.vo.article.vo.ArticleVO;
import top.harrylei.forum.service.article.repository.entity.ArticleDO;

public interface ArticleMapper extends BaseMapper<ArticleDO> {

    @Select("select user_id from article where id = #{articleId} and deleted = 0")
    Long getUserIdByArticleId(Long articleId);

    @Update("update article set deleted = #{deleted} where id = #{articleId}")
    void updateDeleted(Long articleId, Integer deleted);

    @Select("select user_id from article where id = #{articleId}")
    Long getUserIdByArticleIdIncludeDeleted(Long articleId);

    @Update("update article set status = #{status} where id = #{articleId} and deleted = 0")
    Integer updateStatus(Long articleId, Integer status);

    /**
     * 联表查询完整文章VO（包含分类和标签对象）
     *
     * @param articleId 文章ID
     * @return 完整文章VO
     */
    ArticleVO getArticleVoById(Long articleId);

    /**
     * 联表分页查询文章
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<ArticleVO> pageArticleVO(@Param("query") ArticleQueryParam query, IPage<ArticleVO> page);
}
