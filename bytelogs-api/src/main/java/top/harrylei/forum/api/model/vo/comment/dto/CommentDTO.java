package top.harrylei.forum.api.model.vo.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.forum.api.model.entity.BaseDTO;

import java.io.Serial;

/**
 * 评论传输对象
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class CommentDTO extends BaseDTO {

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