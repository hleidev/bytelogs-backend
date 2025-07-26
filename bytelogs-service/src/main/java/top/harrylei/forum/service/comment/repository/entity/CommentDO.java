package top.harrylei.forum.service.comment.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.forum.api.model.base.BaseDO;

import java.io.Serial;

/**
 * 评论实体对象
 *
 * @author harry
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("comment")
@Accessors(chain = true)
public class CommentDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 顶级评论ID
     */
    private Long topCommentId;

    /**
     * 父评论ID
     */
    private Long parentCommentId;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    private Integer deleted;
}