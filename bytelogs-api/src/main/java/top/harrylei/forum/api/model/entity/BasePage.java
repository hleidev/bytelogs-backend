package top.harrylei.forum.api.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 分页基础类
 *
 * @author harry
 */
@Data
@Schema(description = "分页请求参数")
public class BasePage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 默认页码
     */
    public static final int DEFAULT_PAGE_NUM = 1;

    /**
     * 默认每页记录数
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大每页记录数
     */
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * 默认排序映射
     */
    public static final Map<String, String> DEFAULT_SORT_MAPPING = Map.of(
            "createTime", "create_time"
    );

    @Schema(description = "页码，从1开始", defaultValue = "1", example = "1")
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = DEFAULT_PAGE_NUM;

    @Schema(description = "每页大小", defaultValue = "10", example = "10")
    @NotNull(message = "每页大小不能为空")
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = MAX_PAGE_SIZE, message = "每页大小最大为100")
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    /**
     * 排序字符串，格式：field,direction;field,direction
     */
    @Schema(description = "排序字符串，格式：field,direction;field,direction", example = "createTime,desc;userId,asc")
    private String sortField;

    /**
     * 获取字段映射关系
     *
     * @return 字段映射关系
     */
    public Map<String, String> getFieldMapping() {
        return DEFAULT_SORT_MAPPING;
    }
}