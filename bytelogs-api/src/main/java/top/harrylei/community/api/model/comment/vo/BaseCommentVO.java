package top.harrylei.community.api.model.comment.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.community.api.model.base.BaseVO;

/**
 * 评论基础视图对象
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BaseCommentVO extends BaseVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户图像
     */
    private String userAvatar;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数量
     */
    private Integer praiseCount;

    /**
     * true 表示已经点赞
     */
    private Boolean praised;
}