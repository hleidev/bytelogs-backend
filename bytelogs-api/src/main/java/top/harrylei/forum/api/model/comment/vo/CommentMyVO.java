package top.harrylei.forum.api.model.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.forum.api.model.base.BaseVO;

/**
 * 我的评论视图对象
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "我的评论视图对象")
public class CommentMyVO extends BaseVO {

    /**
     * 评论内容
     */
    @Schema(description = "评论内容")
    private String content;

    /**
     * 文章ID
     */
    @Schema(description = "文章ID")
    private Long articleId;

    /**
     * 文章标题
     */
    @Schema(description = "文章标题")
    private String articleTitle;

    /**
     * 父评论ID
     */
    @Schema(description = "父评论ID")
    private Long parentCommentId;

    /**
     * 顶级评论ID
     */
    @Schema(description = "顶级评论ID")
    private Long topCommentId;

    /**
     * 父评论内容
     */
    @Schema(description = "父评论内容")
    private String parentContent;
}