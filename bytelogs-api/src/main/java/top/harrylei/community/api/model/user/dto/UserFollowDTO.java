package top.harrylei.community.api.model.user.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.community.api.model.base.BaseDTO;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.api.enums.user.UserFollowStatusEnum;

/**
 * 用户关注DTO
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserFollowDTO extends BaseDTO {

    /**
     * 关注者用户ID
     */
    private Long userId;

    /**
     * 被关注者用户ID
     */
    private Long followUserId;

    /**
     * 关注状态
     */
    private UserFollowStatusEnum followState;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    private DeleteStatusEnum deleted;
}