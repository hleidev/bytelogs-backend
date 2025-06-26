package top.harrylei.forum.api.model.vo.comment.req;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 评论请求对象
 *
 * @author harry
 */
@Data
public class CommentSaveReq {

    /**
     * 文章ID
     */
    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;

    /**
     * 父评论ID
     */
    private Long parentCommentId;

    /**
     * 回复评论ID，直接回复某条评论时使用
     */
    private Long topCommentId;
}