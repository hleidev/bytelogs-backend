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
 * 标签列表查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "标签列表查询参数")
@Accessors(chain = true)
public class TagQueryParam extends BasePageQuery {

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
            "id", "id",
            "tagName", "tag_name",
            "tagType", "tag_type",
            "categoryId", "category_id",
            "status", "status",
            "createTime", "create_time",
            "updateTime", "update_time"
    );

    /**
     * 有效的排序字段集合
     */
    private static final Set<String> VALID_SORT_FIELDS = FIELD_MAPPING.keySet();

    /**
     * 标签名
     */
    @Schema(description = "标签名")
    private String tagName;

    /**
     * 标签类型：1-系统标签，2-自定义标签
     */
    @Schema(description = "标签类型：1-系统标签，2-自定义标签")
    private Integer tagType;

    /**
     * 所属分类ID
     */
    @Schema(description = "所属分类ID")
    private Long categoryId;

    /**
     * 标签状态
     */
    @Schema(description = "标签状态")
    private Integer status;

    /**
     * 起始时间
     */
    @Schema(description = "创建起始时间", example = "2025-01-01 00:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Schema(description = "创建结束时间", example = "2025-12-31 23:59:59")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 验证排序字段是否合法
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
     *
     * @return 默认排序字段
     */
    @Override
    protected String getDefaultSortField() {
        return DEFAULT_SORT_FIELD;
    }
}