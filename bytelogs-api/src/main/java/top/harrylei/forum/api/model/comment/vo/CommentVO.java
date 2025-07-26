package top.harrylei.forum.api.model.comment.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.forum.api.model.base.BaseVO;

import java.util.List;

/**
 * 评论视图对象
 * 用于API响应的评论数据展示
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CommentVO extends BaseVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论ID，顶级评论为0
     */
    private Long parentCommentId;

    /**
     * 顶级评论ID，直接回复某条评论时使用
     */
    private Long topCommentId;

    /**
     * 点赞数量
     */
    private Integer praiseCount;

    /**
     * 是否已点赞
     */
    private Boolean praised;

    /**
     * 子评论列表
     */
    private List<CommentVO> childComments;
}