package top.harrylei.forum.api.model.exception;

import org.springframework.lang.NonNull;
import top.harrylei.forum.api.model.vo.constants.StatusEnum;

/**
 * 异常工具类
 * <p>
 * 提供便捷方法来在业务代码中抛出异常
 */
public class ExceptionUtil {

    /**
     * 抛出业务异常
     *
     * @param statusEnum 状态枚举
     * @param args 状态消息格式化参数
     */
    public static void error(@NonNull StatusEnum statusEnum, Object... args) {
        throw new ForumException(statusEnum, args);
    }

    /**
     * 抛出业务通知异常
     *
     * @param statusEnum 状态枚举
     * @param args 状态消息格式化参数
     */
    public static void notice(@NonNull StatusEnum statusEnum, Object... args) {
        throw new ForumAdviceException(statusEnum, args);
    }

    /**
     * 条件业务异常：当条件为真时抛出异常
     *
     * @param condition 触发条件
     * @param statusEnum 状态枚举
     * @param args 状态消息格式化参数
     */
    public static void errorIf(boolean condition, @NonNull StatusEnum statusEnum, Object... args) {
        if (condition) {
            error(statusEnum, args);
        }
    }

    /**
     * 检查字符串非空
     *
     * @param str 要检查的字符串
     * @param statusEnum 状态枚举
     * @param args 状态消息格式化参数
     */
    public static void requireNonEmpty(String str, @NonNull StatusEnum statusEnum, Object... args) {
        if (str == null || str.trim().isEmpty()) {
            error(statusEnum, args);
        }
    }

    /**
     * 对象为空时抛出业务异常
     *
     * @param obj        要检查的对象
     * @param statusEnum 状态枚举
     * @param args       状态消息格式化参数
     */
    public static <T> T requireNonNull(T obj, @NonNull StatusEnum statusEnum, Object... args) {
        if (obj == null) {
            error(statusEnum, args);
        }
        return obj;
    }
}