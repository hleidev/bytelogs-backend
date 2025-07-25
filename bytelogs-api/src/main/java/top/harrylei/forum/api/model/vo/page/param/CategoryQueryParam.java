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
 * 分类列表查询参数
 *
 * @author harry
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "分类列表查询参数")
@Accessors(chain = true)
public class CategoryQueryParam extends BasePage {

    /**
     * 前端显示字段到数据库字段的映射
     */
    private static final Map<String, String> FIELD_MAPPING = Map.of(
            "id", "id",
            "categoryName", "category_name",
            "status", "status",
            "sortWeight", "sort",
            "createTime", "create_time",
            "updateTime", "update_time"
    );

    /**
     * 分类名
     */
    @Schema(description = "分类名")
    private String categoryName;

    /**
     * 分类状态
     */
    @Schema(description = "分类状态")
    private Integer status;

    /**
     * 分类排序
     */
    @Schema(description = "分类排序")
    private Integer sortWeight;

    /**
     * 起始时间
     */
    @Schema(description = "注册起始时间", example = "2025-01-01 00:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
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
