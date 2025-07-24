package top.harrylei.forum.api.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
    @Schema(description = "排序字符串，格式：field,direction;field,direction",
            example = "createTime,desc;userId,asc")
    private String sortField;

    /**
     * 解析排序字段
     *
     * @return 有效的排序信息列表
     */
    public List<SortInfo> parseSortFields() {
        List<SortInfo> sortInfos = new ArrayList<>();

        if (StringUtils.hasText(sortField)) {
            String[] sortItems = sortField.split(";");
            for (String sortItem : sortItems) {
                String[] parts = sortItem.trim().split(",");
                if (parts.length == 2) {
                    String field = parts[0].trim();
                    String direction = parts[1].trim();

                    // 验证方向参数的有效性
                    if (StringUtils.hasText(field) && isValidDirection(direction)) {
                        sortInfos.add(new SortInfo(field, direction));
                    }
                }
            }
        }

        return sortInfos;
    }

    /**
     * 验证排序方向是否有效
     */
    private boolean isValidDirection(String direction) {
        return "asc".equalsIgnoreCase(direction) || "desc".equalsIgnoreCase(direction);
    }

    /**
     * 排序信息
     */
    @Data
    @AllArgsConstructor
    public static class SortInfo {
        private String field;
        private String direction;

        public boolean isAsc() {
            return "asc".equalsIgnoreCase(direction);
        }

        public boolean isDesc() {
            return "desc".equalsIgnoreCase(direction);
        }
    }
}