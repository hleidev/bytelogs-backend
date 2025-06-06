package top.harrylei.forum.api.model.vo.page.param;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户列表查询参数
 */
@Data
@Schema(description = "用户列表查询参数")
@Accessors(chain = true)
public class UserQueryParam {

    /**
     * 页码
     */
    @Schema(description = "页码", example = "1", defaultValue = "1")
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    @Schema(description = "每页条数", example = "10", defaultValue = "10")
    private Integer pageSize = 10;

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
    @Schema(description = "注册起始时间", example = "2023-01-01 00:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 注册结束时间
     */
    @Schema(description = "注册结束时间", example = "2023-12-31 23:59:59")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 排序字段，格式：字段1:方向,字段2:方向
     * 例如：userName:asc,createTime:desc
     */
    @Schema(description = "排序参数，格式: 字段:方向,字段:方向", example = "userName:asc,createTime:desc")
    private String sort;

    /**
     * 前端显示字段到数据库字段的映射
     * VO字段名 -> 数据库字段名
     */
    private static final Map<String, String> VO_TO_DB_FIELD_MAPPING = Map.of(
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
     * 有效的排序字段集合（基于VO的字段）
     */
    private static final Set<String> VALID_SORT_FIELDS = VO_TO_DB_FIELD_MAPPING.keySet();

    /**
     * 生成排序SQL子句
     * 
     * @return ORDER BY子句
     */
    public String getOrderBySql() {
        List<String> orderClauses = new ArrayList<>();
        boolean hasCreateTimeSort = false;
        
        if (sort != null && !sort.trim().isEmpty()) {
            String[] sortParts = sort.split(",");
            for (String sortPart : sortParts) {
                String[] fieldDirection = sortPart.trim().split(":");
                if (fieldDirection.length == 2) {
                    String voField = fieldDirection[0].trim();
                    String direction = fieldDirection[1].trim();
                    
                    if (isValidSortField(voField)) {
                        String dbColumn = VO_TO_DB_FIELD_MAPPING.get(voField);
                        String directionSql = "asc".equalsIgnoreCase(direction) ? "ASC" : "DESC";
                        orderClauses.add(dbColumn + " " + directionSql);
                        
                        // 检查是否已经包含了createTime排序
                        if ("createTime".equals(voField)) {
                            hasCreateTimeSort = true;
                        }
                    }
                }
            }
        }
        
        // 如果用户没有指定createTime排序，则添加默认的createTime DESC排序
        if (!hasCreateTimeSort) {
            orderClauses.add("create_time DESC");
        }
        
        // 如果没有任何有效排序字段，使用默认排序
        if (orderClauses.isEmpty()) {
            return "ORDER BY create_time DESC";
        }
        
        return "ORDER BY " + String.join(", ", orderClauses);
    }

    /**
     * 验证排序字段是否合法
     * 
     * @param field VO字段名
     * @return 是否合法
     */
    private boolean isValidSortField(String field) {
        return field != null && VALID_SORT_FIELDS.contains(field);
    }
}