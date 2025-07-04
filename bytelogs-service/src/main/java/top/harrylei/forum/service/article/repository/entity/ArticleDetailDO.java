package top.harrylei.forum.service.article.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.forum.api.model.entity.BaseDO;

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
     * 文章内容
     */
    private String content;


    /**
     * 是否删除：0-未删除，1-已删除
     */
    private Integer deleted;
}