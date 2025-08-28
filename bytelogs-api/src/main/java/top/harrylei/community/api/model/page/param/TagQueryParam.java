package top.harrylei.community.api.model.page.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;
import top.harrylei.community.api.model.base.BasePage;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 标签列表查询参数
 *
 * @author harry
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "标签列表查询参数")
@Accessors(chain = true)
public class TagQueryParam extends BasePage {

    /**
     * 前端显示字段到数据库字段的映射
     */
    private static final Map<String, String> FIELD_MAPPING = Map.of(
            "id", "id",
            "tagName", "tag_name",
            "tagType", "tag_type",
            "createTime", "create_time",
            "updateTime", "update_time"
    );

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
     * 获取字段映射关系
     */
    @Override
    public Map<String, String> getFieldMapping() {
        return FIELD_MAPPING;
    }
}