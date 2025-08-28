package top.harrylei.community.api.model.user.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.community.api.model.base.BasePage;

import java.util.Map;

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

    /**
     * 字段映射关系
     */
    private static final Map<String, String> FIELD_MAPPING = Map.of(
            "followTime", "follow_time",
            "createTime", "follow_time",
            "userName", "user_name",
            "userId", "user_id"
    );

    /**
     * 获取字段映射关系
     */
    @Override
    public Map<String, String> getFieldMapping() {
        return FIELD_MAPPING;
    }
}