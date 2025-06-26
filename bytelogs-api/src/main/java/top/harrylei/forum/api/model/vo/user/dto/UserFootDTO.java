package top.harrylei.forum.api.model.vo.user.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.forum.api.model.entity.BaseDTO;

/**
 * 用户足迹DTO
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserFootDTO extends BaseDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 文章ID
     */
    private Long contentId;

    /**
     * 内容所属的用户ID
     */
    private String contentUserId;

    /**
     * 内容类型：1-文章，2-评论
     */
    private Integer contentType;

    /**
     * 足迹类型：1-点赞，2-收藏，3-关注，4-阅读
     */
    private Integer footType;
}