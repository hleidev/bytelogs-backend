package top.harrylei.forum.api.model.vo.page;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 统一分页结果类
 *
 * @author Harry
 */
@Data
@Schema(description = "分页结果")
public class PageVO<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "当前页码，从1开始")
    private long pageNum;

    @Schema(description = "每页大小")
    private long pageSize;

    @Schema(description = "总页数")
    private long totalPages;

    @Schema(description = "总记录数")
    private long totalElements;

    @Schema(description = "是否有上一页")
    private boolean hasPrevious;

    @Schema(description = "是否有下一页")
    private boolean hasNext;

    @Schema(description = "当前页数据列表")
    private List<T> content;

    /**
     * 创建空的分页结果
     *
     * @param <T> 数据类型
     * @return 空分页结果
     */
    public static <T> PageVO<T> empty() {
        PageVO<T> result = new PageVO<>();
        result.setContent(Collections.emptyList());
        result.setPageNum(1);
        result.setPageSize(10);
        result.setTotalPages(0);
        result.setTotalElements(0);
        result.setHasPrevious(false);
        result.setHasNext(false);
        return result;
    }

    /**
     * 根据查询结果构建分页对象
     *
     * @param content       内容列表
     * @param pageNum       当前页码
     * @param pageSize      每页大小
     * @param totalElements 总记录数
     * @param <T>           数据类型
     * @return 分页结果
     */
    public static <T> PageVO<T> of(List<T> content, long pageNum, long pageSize, long totalElements) {
        PageVO<T> result = new PageVO<>();
        result.setContent(content != null ? content : Collections.emptyList());
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setTotalElements(totalElements);

        // 计算总页数
        long totalPages = pageSize > 0 ? (totalElements + pageSize - 1) / pageSize : 0;
        result.setTotalPages(totalPages);

        // 设置分页导航属性
        result.setHasPrevious(pageNum > 1);
        result.setHasNext(pageNum < totalPages);

        return result;
    }

    /**
     * 从MyBatis-Plus IPage对象转换
     *
     * @param iPage MyBatis-Plus IPage对象
     * @param <T>   数据类型
     * @return 分页结果
     */
    public static <T> PageVO<T> from(IPage<T> iPage) {
        if (iPage == null) {
            return empty();
        }

        PageVO<T> result = new PageVO<>();
        result.setContent(iPage.getRecords() != null ? iPage.getRecords() : Collections.emptyList());
        result.setPageNum(iPage.getCurrent());
        result.setPageSize(iPage.getSize());
        result.setTotalElements(iPage.getTotal());
        result.setTotalPages(iPage.getPages());

        // 设置分页导航属性
        result.setHasPrevious(iPage.getCurrent() > 1);
        result.setHasNext(iPage.getCurrent() < iPage.getPages());

        return result;
    }
}