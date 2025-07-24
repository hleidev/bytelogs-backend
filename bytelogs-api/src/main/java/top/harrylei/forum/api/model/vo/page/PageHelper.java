package top.harrylei.forum.api.model.vo.page;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import top.harrylei.forum.api.model.entity.BasePage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页工具类
 *
 * @author harry
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

    /**
     * 根据MyBatis-Plus对象构建分页结果并进行数据转换
     *
     * @param iPage     MyBatis-Plus IPage对象
     * @param converter 数据转换函数
     * @param <T>       原始数据类型
     * @param <R>       目标数据类型
     * @return 转换后的分页结果
     */
    public static <T, R> PageVO<R> buildAndMap(IPage<T> iPage, Function<T, R> converter) {
        if (iPage == null) {
            return empty();
        }

        // 转换数据
        List<R> convertedRecords = iPage.getRecords().stream()
                .map(converter)
                .toList();

        // 创建新的IPage对象，复制分页信息
        IPage<R> convertedPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(iPage.getCurrent(),
                                                                                                  iPage.getSize(),
                                                                                                  iPage.getTotal());
        convertedPage.setRecords(convertedRecords);

        // 复用现有的build方法
        return build(convertedPage);
    }

    /**
     * 根据 BasePage 创建 MyBatis-Plus 分页对象
     *
     * @param basePage 分页参数
     * @param <T>      分页数据类型
     * @return MyBatis-Plus 分页对象
     */
    public static <T> IPage<T> createPage(BasePage basePage) {
        return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                basePage.getPageNum(),
                basePage.getPageSize()
        );
    }

    /**
     * 根据 BasePage 创建 MyBatis-Plus 分页对象
     * 默认按 create_time 降序排列
     *
     * @param basePage     分页参数
     * @param fieldMapping 字段映射关系
     * @param <T>          分页数据类型
     * @return MyBatis-Plus 分页对象
     */
    public static <T> IPage<T> createPage(BasePage basePage, Map<String, String> fieldMapping) {
        String defaultSortField = BasePage.DEFAULT_SORT_MAPPING.get("createTime");
        return createPage(basePage, fieldMapping, OrderItem.desc(defaultSortField));
    }

    /**
     * 根据 BasePage 创建 MyBatis-Plus 分页对象
     *
     * @param basePage      分页参数
     * @param fieldMapping  字段映射关系
     * @param defaultOrders 默认排序项
     * @param <T>           分页数据类型
     * @return MyBatis-Plus 分页对象
     */
    public static <T> IPage<T> createPage(BasePage basePage,
                                          Map<String, String> fieldMapping,
                                          OrderItem... defaultOrders) {
        // FIXME: 为避免类冲突，暂时使用分页类全限定名
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                basePage.getPageNum(),
                basePage.getPageSize());

        // 解析并设置排序
        List<OrderItem> orderItems = parseOrderItems(basePage, fieldMapping);
        if (orderItems.isEmpty() && defaultOrders.length > 0) {
            page.addOrder(Arrays.asList(defaultOrders));
        } else if (!orderItems.isEmpty()) {
            page.addOrder(orderItems);
        }

        return page;
    }

    /**
     * 解析排序项
     *
     * @param basePage     分页参数
     * @param fieldMapping 字段映射关系
     * @return 有效的排序项列表
     */
    private static List<OrderItem> parseOrderItems(BasePage basePage, Map<String, String> fieldMapping) {
        List<OrderItem> orderItems = new ArrayList<>();
        List<BasePage.SortInfo> sortInfos = basePage.parseSortFields();

        for (BasePage.SortInfo sortInfo : sortInfos) {
            // 只有在映射表中存在的字段才允许排序，防止SQL注入
            String column = fieldMapping.get(sortInfo.getField());
            if (column != null) {
                OrderItem orderItem = sortInfo.isAsc() ? OrderItem.asc(column) : OrderItem.desc(column);
                orderItems.add(orderItem);
            }
        }

        return orderItems;
    }
}