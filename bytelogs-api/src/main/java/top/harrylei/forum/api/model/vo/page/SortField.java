package top.harrylei.forum.api.model.vo.page;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 排序字段配置
 * <p>
 * 用于配置查询时的排序字段和排序方向
 * </p>
 */
@Data
@Schema(description = "排序字段配置")
@Accessors(chain = true)
public class SortField implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段名称", example = "createTime")
    private String field;

    /**
     * 排序方向
     */
    @Schema(description = "排序方向", example = "desc", allowableValues = {"asc", "desc"})
    private String direction = "desc";

    /**
     * 创建排序字段
     *
     * @param field 字段名
     * @param direction 排序方向
     * @return 排序字段
     */
    public static SortField of(String field, String direction) {
        return new SortField().setField(field).setDirection(direction);
    }

    /**
     * 创建升序排序字段
     *
     * @param field 字段名
     * @return 升序排序字段
     */
    public static SortField asc(String field) {
        return of(field, "asc");
    }

    /**
     * 创建降序排序字段
     *
     * @param field 字段名
     * @return 降序排序字段
     */
    public static SortField desc(String field) {
        return of(field, "desc");
    }
}