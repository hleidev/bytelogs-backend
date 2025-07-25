package top.harrylei.forum.api.model.comment.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import top.harrylei.forum.api.validation.SecureContent.ContentSecurityType;
import top.harrylei.forum.api.validation.SecureContent;

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
    @Size(min = 1, max = 500, message = "评论内容长度必须在1-500字符之间")
    @SecureContent(contentType = ContentSecurityType.PLAIN_TEXT, allowEmpty = false)
    private String content;

    /**
     * 父评论ID
     */
    private Long parentCommentId;
}