package top.harrylei.community.api.model.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.community.api.model.base.BaseVO;
import top.harrylei.community.api.enums.YesOrNoEnum;

/**
 * 评论管理VO
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "评论管理VO")
public class CommentManagementVO extends BaseVO {

    /**
     * 评论内容
     */
    @Schema(description = "评论内容")
    private String content;

    /**
     * 评论者用户ID
     */
    @Schema(description = "评论者用户ID")
    private Long userId;

    /**
     * 评论者用户名
     */
    @Schema(description = "评论者用户名")
    private String userName;

    /**
     * 关联文章ID
     */
    @Schema(description = "关联文章ID")
    private Long articleId;

    /**
     * 关联文章标题
     */
    @Schema(description = "关联文章标题")
    private String articleTitle;

    /**
     * 顶级评论ID，0表示顶级评论
     */
    @Schema(description = "顶级评论ID，0表示顶级评论")
    private Long topCommentId;

    /**
     * 父评论ID，0表示顶级评论
     */
    @Schema(description = "父评论ID，0表示顶级评论")
    private Long parentCommentId;

    /**
     * 删除状态：0-未删除，1-已删除
     */
    @Schema(description = "删除状态：0-未删除，1-已删除")
    private YesOrNoEnum deleted;
}