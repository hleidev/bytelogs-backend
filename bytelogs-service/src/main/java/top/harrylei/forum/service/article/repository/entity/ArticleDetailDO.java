package top.harrylei.forum.service.article.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.forum.api.model.base.BaseDO;

import java.io.Serial;

/**
 * 文章详情实体对象
 *
 * @author harry
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article_detail")
@Accessors(chain = true)
public class ArticleDetailDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 短标题
     */
    private String shortTitle;

    /**
     * 文章头图
     */
    private String picture;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 类目ID
     */
    private Long categoryId;

    /**
     * 来源：1-转载，2-原创，3-翻译
     */
    private Integer source;

    /**
     * 原文链接
     */
    private String sourceUrl;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 状态：0-草稿，1-已发布，2-待审核，3-审核拒绝
     */
    private Integer status;

    /**
     * 最新版本标记：0-否，1-是
     */
    private Integer latest;

    /**
     * 发布版本标记：0-否，1-是
     */
    private Integer published;

    /**
     * 发布时间
     */
    private java.time.LocalDateTime publishTime;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    private Integer deleted;
}