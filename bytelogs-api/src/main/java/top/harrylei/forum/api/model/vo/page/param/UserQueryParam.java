package top.harrylei.forum.api.model.vo.page.param;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 用户列表查询参数
 * <p>
 * 用于管理员查询用户列表，支持多条件查询和排序
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户列表查询参数")
@Accessors(chain = true)
public class UserQueryParam extends BasePageQuery {

    /**
     * 默认排序字段
     */
    private static final String DEFAULT_SORT_FIELD = "createTime";
    
    /**
     * 默认数据库排序字段
     */
    private static final String DEFAULT_DB_SORT_COLUMN = "create_time";

    /**
     * 前端显示字段到数据库字段的映射
     */
    private static final Map<String, String> FIELD_MAPPING = Map.of(
            "userId", "id",
            "userName", "user_name",
            "email", "email",
            "status", "status",
            "createTime", "create_time",
            "updateTime", "update_time",
            "company", "company",
            "position", "position",
            "profile", "profile",
            "deleted", "deleted"
    );

    /**
     * 有效的排序字段集合
     */
    private static final Set<String> VALID_SORT_FIELDS = FIELD_MAPPING.keySet();

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
     * 验证排序字段是否合法
     * <p>
     * 检查字段是否在允许的排序字段集合中
     * </p>
     *
     * @param field 字段名
     * @return 是否为合法排序字段
     */
    @Override
    protected boolean isValidSortField(String field) {
        return field != null && VALID_SORT_FIELDS.contains(field);
    }

    /**
     * 获取字段对应的数据库列名
     * <p>
     * 从映射表中获取对应的数据库列名，如果不存在则返回默认排序字段
     * </p>
     *
     * @param field 字段名
     * @return 数据库列名
     */
    @Override
    protected String getSortColumn(String field) {
        if (field == null || !VALID_SORT_FIELDS.contains(field)) {
            return DEFAULT_DB_SORT_COLUMN;
        }
        return FIELD_MAPPING.get(field);
    }

    /**
     * 获取默认排序字段
     * <p>
     * 返回默认的排序字段名
     * </p>
     *
     * @return 默认排序字段
     */
    @Override
    protected String getDefaultSortField() {
        return DEFAULT_SORT_FIELD;
    }
}