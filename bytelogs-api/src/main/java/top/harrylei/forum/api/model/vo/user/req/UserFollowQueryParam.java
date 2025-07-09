package top.harrylei.forum.api.model.vo.user.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.forum.api.model.entity.BasePage;

/**
 * 用户关注查询参数
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户关注查询参数")
public class UserFollowQueryParam extends BasePage {

    /**
     * 关注者用户ID
     */
    @Schema(description = "关注者用户ID", example = "1")
    private Long userId;

    /**
     * 被关注者用户ID
     */
    @Schema(description = "被关注者用户ID", example = "2")
    private Long followUserId;

    /**
     * 用户名关键词（模糊搜索）
     */
    @Schema(description = "用户名关键词", example = "张三")
    private String userName;
}