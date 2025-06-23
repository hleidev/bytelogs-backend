package top.harrylei.forum.core.exception;

import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import top.harrylei.forum.api.model.enums.ErrorCodeEnum;

import java.util.Collection;

/**
 * 异常工具类
 *
 * @author harry
 */
public class ExceptionUtil {

    /**
     * 抛出业务异常
     *
     * @param errorCodeEnum 状态枚举
     * @param args          状态消息格式化参数
     */
    public static void error(@NonNull ErrorCodeEnum errorCodeEnum, Object... args) {
        throw new ForumException(errorCodeEnum, args);
    }

    /**
     * 条件业务异常：当条件为真时抛出异常
     *
     * @param condition     触发条件
     * @param errorCodeEnum 状态枚举
     * @param args          状态消息格式化参数
     */
    public static void errorIf(boolean condition, @NonNull ErrorCodeEnum errorCodeEnum, Object... args) {
        if (condition) {
            error(errorCodeEnum, args);
        }
    }

    /**
     * 校验对象有效性，根据对象类型进行不同的校验逻辑
     * <p>
     * 校验规则：
     * - String: 非空且非空白字符串
     * - Collection: 非空且包含元素
     * - Number: 非空且为正数（排除NaN和无穷大）
     * - Array: 非空且长度大于0
     * - 其他对象: 非空
     *
     * @param obj           要检查的对象
     * @param errorCodeEnum 状态枚举
     * @param args          状态消息格式化参数
     */
    public static void requireValid(Object obj, @NonNull ErrorCodeEnum errorCodeEnum, Object... args) {
        if (obj == null) {
            error(errorCodeEnum, args);
        }

        // 针对不同类型进行额外校验
        try {
            if (obj instanceof String str) {
                if (str.trim().isEmpty()) {
                    error(errorCodeEnum, args);
                }
            } else if (obj instanceof Collection<?> collection) {
                if (CollectionUtils.isEmpty(collection)) {
                    error(errorCodeEnum, args);
                }
            } else if (obj instanceof Number number) {
                double value = number.doubleValue();
                // 检查NaN、无穷大和非正数
                if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0) {
                    error(errorCodeEnum, args);
                }
            } else if (obj.getClass().isArray()) {
                // 使用反射获取数组长度，避免类型转换问题
                int length = java.lang.reflect.Array.getLength(obj);
                if (length == 0) {
                    error(errorCodeEnum, args);
                }
            }
        } catch (Exception e) {
            // 如果在类型转换或校验过程中出现异常，认为校验失败
            error(errorCodeEnum, args);
        }
    }

    /**
     * 检查字符串非空
     *
     * @deprecated 使用 {@link #requireValid(Object, ErrorCodeEnum, Object...)} 替代
     */
    @Deprecated
    public static void requireNonEmpty(String str, @NonNull ErrorCodeEnum errorCodeEnum, Object... args) {
        requireValid(str, errorCodeEnum, args);
    }
}