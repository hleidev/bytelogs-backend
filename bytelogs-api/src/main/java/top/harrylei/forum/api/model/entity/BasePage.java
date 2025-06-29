package top.harrylei.forum.api.model.entity;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.util.StringUtils;

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
     * 创建带排序的分页对象
     *
     * @param fieldMapping 字段映射关系
     * @param <T>          分页数据类型
     * @return 带排序的分页对象
     */
    public <T> IPage<T> toPage(Map<String, String> fieldMapping) {
        Page<T> page = new Page<>(this.pageNum, this.pageSize);

        if (StringUtils.hasText(sortField)) {
            // 解析排序字符串，格式：field,direction;field,direction
            boolean hasValidOrder = false;
            String[] sortItems = sortField.split(";");
            for (String sortItem : sortItems) {
                String[] parts = sortItem.trim().split(",");
                if (parts.length == 2) {
                    String field = parts[0].trim();
                    String direction = parts[1].trim();
                    
                    String column = fieldMapping.get(field);
                    if (column != null && StringUtils.hasText(direction)) {
                        boolean isAsc = "asc".equalsIgnoreCase(direction);
                        page.addOrder(isAsc ? OrderItem.asc(column) : OrderItem.desc(column));
                        hasValidOrder = true;
                    }
                }
            }

            // 如果没有有效排序，使用默认排序
            if (!hasValidOrder) {
                page.addOrder(OrderItem.desc("create_time"));
            }
        } else {
            // 默认排序：创建时间倒序
            page.addOrder(OrderItem.desc("create_time"));
        }

        return page;
    }
}