package top.harrylei.forum.core.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import org.springframework.util.StringUtils;
import top.harrylei.forum.api.model.entity.BasePage;
import top.harrylei.forum.api.model.vo.page.PageVO;

import java.util.*;
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
     *
     * @param basePage   分页参数
     * @param enableSort 是否启用排序
     * @param <T>        分页数据类型
     * @return MyBatis-Plus 分页对象
     */
    public static <T> IPage<T> createPage(BasePage basePage, boolean enableSort) {
        String defaultSortField = BasePage.DEFAULT_SORT_MAPPING.get("createTime");
        return createPage(basePage, enableSort, OrderItem.desc(defaultSortField));
    }

    /**
     * 根据 BasePage 创建 MyBatis-Plus 分页对象
     *
     * @param basePage      分页参数
     * @param enableSort    是否启用排序
     * @param defaultOrders 默认排序项（当没有指定排序时使用）
     * @param <T>           分页数据类型
     * @return MyBatis-Plus 分页对象
     */
    public static <T> IPage<T> createPage(BasePage basePage, boolean enableSort, OrderItem... defaultOrders) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                        basePage.getPageNum(),
                        basePage.getPageSize());

        if (enableSort) {
            // 使用BasePage对象的字段映射进行排序解析
            Map<String, String> fieldMapping = basePage.getFieldMapping();
            List<OrderItem> orderItems = parseOrderItems(basePage, fieldMapping);

            if (orderItems.isEmpty()) {
                // 如果没有指定排序，使用传入的默认排序或系统默认排序
                if (defaultOrders != null && defaultOrders.length > 0) {
                    // 过滤掉null的OrderItem，防止NullPointerException
                    List<OrderItem> validOrders = Arrays.stream(defaultOrders)
                            .filter(Objects::nonNull)
                            .toList();
                    if (!validOrders.isEmpty()) {
                        page.addOrder(validOrders);
                    }
                } else {
                    // 使用系统默认排序：按创建时间降序
                    String defaultSortField = fieldMapping.get("createTime");
                    if (defaultSortField != null) {
                        page.addOrder(OrderItem.desc(defaultSortField));
                    }
                }
            } else {
                page.addOrder(orderItems);
            }
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
        String sortField = basePage.getSortField();

        if (StringUtils.hasText(sortField)) {
            String[] sortItems = sortField.split(";");
            for (String sortItem : sortItems) {
                String[] parts = sortItem.trim().split(",");
                if (parts.length == 2) {
                    String field = parts[0].trim();
                    String direction = parts[1].trim();

                    // 验证方向参数的有效性和字段映射存在性
                    if (StringUtils.hasText(field) && isValidDirection(direction)) {
                        String column = fieldMapping.get(field);
                        if (column != null) {
                            OrderItem orderItem = "asc".equalsIgnoreCase(direction)
                                    ? OrderItem.asc(column)
                                    : OrderItem.desc(column);
                            orderItems.add(orderItem);
                        }
                    }
                }
            }
        }

        return orderItems;
    }

    /**
     * 验证排序方向是否有效
     */
    private static boolean isValidDirection(String direction) {
        return "asc".equalsIgnoreCase(direction) || "desc".equalsIgnoreCase(direction);
    }
}