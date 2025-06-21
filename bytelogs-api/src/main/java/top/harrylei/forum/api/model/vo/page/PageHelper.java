package top.harrylei.forum.api.model.vo.page;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页工具类
 *
 * @author Harry
 */
public class PageHelper {

    private PageHelper() {
        // 工具类不允许实例化
    }

    /**
     * 空分页结果
     *
     * @param <T> 结果类型
     * @return 空的分页结果
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
     * 对分页结果进行数据转换
     *
     * @param pageVO    原始分页结果
     * @param converter 数据转换函数
     * @param <T>       原始数据类型
     * @param <R>       目标数据类型
     * @return 转换后的分页结果
     */
    public static <T, R> PageVO<R> map(PageVO<T> pageVO, Function<T, R> converter) {
        if (pageVO == null) {
            return empty();
        }

        List<R> convertedContent = pageVO.getContent().stream().map(converter).collect(Collectors.toList());

        PageVO<R> result = new PageVO<>();
        result.setContent(convertedContent);
        result.setPageNum(pageVO.getPageNum());
        result.setPageSize(pageVO.getPageSize());
        result.setTotalPages(pageVO.getTotalPages());
        result.setTotalElements(pageVO.getTotalElements());
        result.setHasPrevious(pageVO.isHasPrevious());
        result.setHasNext(pageVO.isHasNext());

        return result;
    }

    /**
     * 创建分页请求
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页请求
     */
    @Deprecated
    public static Page createPage(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) {
            pageNum = Page.DEFAULT_PAGE_NUM;
        }

        if (pageSize == null || pageSize < 1) {
            pageSize = Page.DEFAULT_PAGE_SIZE;
        } else if (pageSize > Page.MAX_PAGE_SIZE) {
            pageSize = Page.MAX_PAGE_SIZE;
        }

        return Page.of(pageNum, pageSize);
    }

    /**
     * 构建分页结果
     *
     * @param content       内容列表
     * @param pageNum       当前页码
     * @param pageSize      每页大小
     * @param totalElements 总记录数
     * @param <T>           数据类型
     * @return 分页结果
     */
    public static <T> PageVO<T> build(List<T> content, long pageNum, long pageSize, long totalElements) {
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
     * 根据MyBatis-Plus对象转换构建分页结果
     *
     * @param iPage MyBatis-Plus IPage对象
     * @param <T>   数据类型
     * @return 分页结果
     */
    public static <T> PageVO<T> build(IPage<T> iPage) {
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