package top.harrylei.forum.api.model.vo.page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页工具类
 * <p>
 * 提供分页数据转换、处理的常用工具方法
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
        return PageVO.empty();
    }

    /**
     * 对分页结果进行数据转换
     *
     * @param pageVO 原始分页结果
     * @param converter 数据转换函数
     * @param <T> 原始数据类型
     * @param <R> 目标数据类型
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
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页请求
     */
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
     * @param list 数据列表
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @param total 总记录数
     * @param <T> 数据类型
     * @return 分页结果
     */
    public static <T> PageVO<T> build(List<T> list, int pageNum, int pageSize, long total) {
        return PageVO.of(list, pageNum, pageSize, total);
    }
}