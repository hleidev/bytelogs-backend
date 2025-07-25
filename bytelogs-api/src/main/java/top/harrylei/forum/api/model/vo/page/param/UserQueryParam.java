package top.harrylei.forum.api.model.vo.page.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;
import top.harrylei.forum.api.model.entity.BasePage;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户列表查询参数
 *
 * @author harry
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户列表查询参数")
@Accessors(chain = true)
public class UserQueryParam extends BasePage {

    /**
     * 前端显示字段到数据库字段的映射
     */
    private static final Map<String, String> FIELD_MAPPING = Map.of(
            "userId", "id",
            "userName", "user_name",
            "email", "email",
            "status", "status",
            "role", "user_role",
            "createTime", "create_time",
            "updateTime", "update_time",
            "deleted", "deleted"
    );

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String userName;

    /**
     * 用户状态
     */
    @Schema(description = "用户状态")
    private Integer status;

    /**
     * 删除标识
     */
    @Schema(description = "是否删除")
    private Integer deleted;

    /**
     * 注册起始时间
     */
    @Schema(description = "注册起始时间", example = "2025-01-01 00:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 注册结束时间
     */
    @Schema(description = "注册结束时间", example = "2025-12-31 23:59:59")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 获取字段映射关系
     */
    @Override
    public Map<String, String> getFieldMapping() {
        return FIELD_MAPPING;
    }
}