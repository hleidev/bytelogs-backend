package top.harrylei.community.service.user.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.api.enums.user.UserFollowStatusEnum;
import top.harrylei.community.api.model.base.BaseDO;

import java.io.Serial;

/**
 * 用户关注实体对象
 *
 * @author harry
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_relation")
@Accessors(chain = true)
public class UserFollowDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关注者用户ID
     */
    private Long userId;

    /**
     * 被关注者用户ID
     */
    private Long followUserId;

    /**
     * 关注状态：0-未关注，1-已关注
     */
    private UserFollowStatusEnum followState;

    /**
     * 删除标记：0-正常，1-已删除
     */
    private DeleteStatusEnum deleted;
}