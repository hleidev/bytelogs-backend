package top.harrylei.forum.api.model.vo.page.param;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 基础分页查询参数
 * <p>
 * 包含分页、排序的基础查询参数
 * </p>
 */
@Data
public abstract class BasePageQuery {

    /**
     * SQL排序关键字
     */
    private static final String SQL_ORDER_ASC = "ASC";
    private static final String SQL_ORDER_DESC = "DESC";
    
    /**
     * SQL语句分隔符
     */
    private static final String SQL_COMMA = ", ";
    private static final String SQL_SPACE = " ";
    private static final String SQL_ORDER_BY = "ORDER BY";

    @Schema(description = "页码", example = "1", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数", example = "10", defaultValue = "10")
    private Integer pageSize = 10;

    @Schema(description = "排序参数，格式: 字段名,排序方向", example = "createTime,desc")
    private String sort;

    /**
     * 获取排序条件列表
     * 
     * @return 排序条件列表
     */
    public List<OrderItem> getOrders() {
        List<OrderItem> orders = new ArrayList<>();
        
        // 如果排序参数为空，返回默认排序
        if (sort == null || sort.trim().isEmpty()) {
            orders.add(defaultOrder());
            return orders;
        }
        
        // 解析排序参数
        String[] sortParams = sort.split(";");
        for (String param : sortParams) {
            String[] parts = param.split(",");
            if (parts.length >= 2) {
                String field = parts[0].trim();
                String direction = parts[1].trim();
                
                // 验证排序字段
                if (isValidSortField(field)) {
                    OrderItem orderItem = new OrderItem()
                            .setColumn(getSortColumn(field))
                            .setAsc(isAscendingDirection(direction));
                    orders.add(orderItem);
                }
            }
        }
        
        // 如果没有有效排序条件，使用默认排序
        if (orders.isEmpty()) {
            orders.add(defaultOrder());
        } else if (isAlwaysAppendDefaultSort() && !hasSortField(orders, getDefaultSortField())) {
            // 如果需要始终添加默认排序且当前排序中没有默认字段，添加默认排序
            orders.add(defaultOrder());
        }
        
        return orders;
    }
    
    /**
     * 检查排序条件列表中是否包含指定字段
     */
    private boolean hasSortField(List<OrderItem> orders, String field) {
        return orders.stream()
                .anyMatch(item -> getSortColumn(field).equals(item.getColumn()));
    }
    
    /**
     * 获取默认排序条件
     */
    protected OrderItem defaultOrder() {
        return new OrderItem()
                .setColumn(getSortColumn(getDefaultSortField()))
                .setAsc(isDefaultSortAsc());
    }
    
    /**
     * 获取排序SQL子句
     */
    public String getOrderBySql() {
        List<OrderItem> orders = getOrders();
        List<String> clauses = new ArrayList<>();
        
        for (OrderItem item : orders) {
            clauses.add(item.getColumn() + SQL_SPACE + 
                       (item.isAsc() ? SQL_ORDER_ASC : SQL_ORDER_DESC));
        }
        
        return SQL_ORDER_BY + SQL_SPACE + String.join(SQL_COMMA, clauses);
    }
    
    /**
     * 验证排序字段是否合法
     * 
     * @param field 字段名
     * @return 是否为合法排序字段
     */
    protected abstract boolean isValidSortField(String field);
    
    /**
     * 获取字段对应的数据库列名
     * 
     * @param field 字段名
     * @return 数据库列名
     */
    protected abstract String getSortColumn(String field);
    
    /**
     * 获取默认排序字段
     * 
     * @return 默认排序字段
     */
    protected abstract String getDefaultSortField();
    
    /**
     * 默认排序方向是否为升序
     * 
     * @return 是否升序
     */
    protected boolean isDefaultSortAsc() {
        return false;
    }
    
    /**
     * 是否始终添加默认排序
     * 
     * @return 是否始终添加默认排序
     */
    protected boolean isAlwaysAppendDefaultSort() {
        return true;
    }
    
    /**
     * 排序项
     */
    @Data
    @Schema(description = "排序项")
    public static class OrderItem {
        
        @Schema(description = "列名")
        private String column;
        
        @Schema(description = "是否升序")
        private boolean asc;
        
        public OrderItem setColumn(String column) {
            this.column = column;
            return this;
        }
        
        public OrderItem setAsc(boolean asc) {
            this.asc = asc;
            return this;
        }
    }

    /**
     * 判断用户输入的方向是否为升序
     * 
     * @param direction 用户输入的方向
     * @return 是否为升序
     */
    private boolean isAscendingDirection(String direction) {
        return SQL_ORDER_ASC.equalsIgnoreCase(direction);
    }
}