package top.harrylei.forum.core.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import top.harrylei.forum.api.model.base.BasePage;
import top.harrylei.forum.api.model.page.PageVO;

import java.util.*;
import java.util.function.Function;

/**
 * 分页工具类
 *
 * @author harry
 */
public class PageUtils {

    private PageUtils() {
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

        List<R> convertedContent = pageVO.getContent().stream().map(converter).toList();

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
     * 根据MyBatis-Plus对象转换构建分页结果
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

    /**
     * 根据MyBatis-Plus对象构建分页结果并进行数据转换
     *
     * @param iPage     MyBatis-Plus IPage对象
     * @param converter 数据转换函数
     * @param <T>       原始数据类型
     * @param <R>       目标数据类型
     * @return 转换后的分页结果
     */
    public static <T, R> PageVO<R> from(IPage<T> iPage, Function<T, R> converter) {
        if (iPage == null) {
            return empty();
        }

        // 转换数据
        List<R> convertedRecords = iPage.getRecords().stream()
                .map(converter)
                .toList();

        // 创建新的IPage对象，复制分页信息
        IPage<R> convertedPage = new Page<>(iPage.getCurrent(), iPage.getSize(), iPage.getTotal());
        convertedPage.setRecords(convertedRecords);

        // 复用现有的build方法
        return from(convertedPage);
    }

    /**
     * 根据MyBatis-Plus对象构建分页结果并进行列表转换
     *
     * @param iPage         MyBatis-Plus IPage对象
     * @param listConverter 列表转换函数
     * @param <T>           原始数据类型
     * @param <R>           目标数据类型
     * @return 转换后的分页结果
     */
    public static <T, R> PageVO<R> fromList(IPage<T> iPage, Function<List<T>, List<R>> listConverter) {
        if (iPage == null || iPage.getRecords().isEmpty()) {
            return empty();
        }

        // 使用列表转换函数转换数据
        List<R> convertedRecords = listConverter.apply(iPage.getRecords());

        // 创建新的IPage对象，复制分页信息
        IPage<R> convertedPage = new Page<>(iPage.getCurrent(), iPage.getSize(), iPage.getTotal());
        convertedPage.setRecords(convertedRecords);

        // 复用现有的from方法
        return from(convertedPage);
    }

    /**
     * 创建空分页对象
     *
     * @param <T> 分页数据类型
     * @return 空的MyBatis-Plus分页对象
     */
    public static <T> IPage<T> of() {
        return new Page<>(1, 10);
    }

    /**
     * 创建简单分页对象
     *
     * @param pageNum  页码（从1开始）
     * @param pageSize 每页大小
     * @param <T>      分页数据类型
     * @return MyBatis-Plus分页对象
     */
    public static <T> IPage<T> of(long pageNum, long pageSize) {
        return new Page<>(pageNum, pageSize);
    }

    /**
     * 创建分页对象
     *
     * @param basePage 分页参数
     * @param <T>      分页数据类型
     * @return MyBatis-Plus分页对象
     */
    public static <T> IPage<T> of(BasePage basePage) {
        if (basePage == null) {
            return of();
        }

        // 使用字段映射自动推导默认排序
        Map<String, String> fieldMapping = basePage.getFieldMapping();
        OrderItem[] defaultOrders = null;
        if (fieldMapping != null) {
            String createTimeColumn = fieldMapping.get("createTime");
            if (createTimeColumn != null) {
                defaultOrders = new OrderItem[]{OrderItem.desc(createTimeColumn)};
            }
        }

        // 复用带默认排序的方法
        return of(basePage, defaultOrders);
    }

    /**
     * 创建分页对象
     *
     * @param basePage      分页参数
     * @param defaultOrders 默认排序项
     * @param <T>           分页数据类型
     * @return MyBatis-Plus分页对象
     */
    public static <T> IPage<T> of(BasePage basePage, OrderItem... defaultOrders) {
        if (basePage == null) {
            return of();
        }

        Page<T> page = new Page<>(basePage.getPageNum(), basePage.getPageSize());

        // 优先尝试使用用户指定的排序
        Map<String, String> fieldMapping = basePage.getFieldMapping();
        if (fieldMapping != null && StringUtils.isNotBlank(basePage.getSortField())) {
            List<OrderItem> userOrderItems = parseOrderItems(basePage, fieldMapping);
            if (!userOrderItems.isEmpty()) {
                page.addOrder(userOrderItems);
                return page;
            }
        }

        // 使用指定的默认排序
        addValidOrders(page, defaultOrders);

        return page;
    }


    /**
     * 添加有效的排序项
     */
    private static void addValidOrders(Page<?> page, OrderItem... orders) {
        if (orders == null || orders.length == 0) {
            return;
        }

        List<OrderItem> validOrders = Arrays.stream(orders)
                .filter(Objects::nonNull)
                .toList();

        if (!validOrders.isEmpty()) {
            page.addOrder(validOrders);
        }
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
        if (StringUtils.isNotBlank(sortField)) {
            String[] sortItems = sortField.split(";");
            for (String sortItem : sortItems) {
                String[] parts = sortItem.trim().split(",");
                if (parts.length == 2) {
                    String field = parts[0].trim();
                    String direction = parts[1].trim();

                    // 验证方向参数的有效性和字段映射存在性
                    if (StringUtils.isNotBlank(field) && isValidDirection(direction)) {
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