package top.harrylei.forum.api.model.vo.comment.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.forum.api.model.entity.BaseDTO;

/**
 * 评论树状结构
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BaseCommentDTO extends BaseDTO {

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
