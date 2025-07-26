package top.harrylei.forum.service.article.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.forum.api.model.base.BaseDO;

/**
 * 文章标签映射表实体对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("article_tag")
@Accessors(chain = true)
public class ArticleTagDO extends BaseDO {

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 标签ID
     */
    private Long tagId;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    private Integer deleted;
}