package top.harrylei.forum.api.model.vo.page;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一分页请求参数
 * <p>
 * 标准化分页查询的请求参数，支持参数校验
 */
@Data
@Schema(description = "分页请求参数")
public class Page implements Serializable {

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

    @Schema(description = "页码，从1开始", defaultValue = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = DEFAULT_PAGE_NUM;

    @Schema(description = "每页大小", defaultValue = "10")
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = MAX_PAGE_SIZE, message = "每页大小最大为100")
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    /**
     * 获取开始位置
     *
     * @return 开始位置索引（从0开始）
     */
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }

    /**
     * 获取查询条数
     *
     * @return 查询条数
     */
    public int getLimit() {
        return pageSize;
    }

    /**
     * 创建一个默认的分页请求
     *
     * @return 默认分页请求
     */
    public static Page of() {
        return new Page();
    }

    /**
     * 创建指定页码和大小的分页请求
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页请求
     */
    public static Page of(int pageNum, int pageSize) {
        Page request = new Page();
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        return request;
    }

    /**
     * 生成MySQL分页SQL的limit语句
     *
     * @return limit语句
     */
    public String getLimitSql() {
        return String.format("LIMIT %d, %d", getOffset(), getLimit());
    }
}