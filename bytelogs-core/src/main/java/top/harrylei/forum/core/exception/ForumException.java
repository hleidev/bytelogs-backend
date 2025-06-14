package top.harrylei.forum.core.exception;

import lombok.Getter;
import top.harrylei.forum.api.model.enums.ErrorCodeEnum;

import java.io.Serial;

/**
 * 业务异常基类
 * <p>
 * 用于表示可预期的业务逻辑异常，会记录警告日志
 */
@Getter
public class ForumException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -7034897190745766929L;

    /**
     * 状态枚举
     */
    private final ErrorCodeEnum errorCodeEnum;
    
    /**
     * 错误消息格式化参数
     */
    private final Object[] args;

    public ForumException(ErrorCodeEnum errorCodeEnum, Object... args) {
        super(formatMessage(errorCodeEnum.getMessage(), args));
        this.errorCodeEnum = errorCodeEnum;
        this.args = args;
    }
    
    /**
     * 格式化错误消息
     */
    private static String formatMessage(String message, Object... args) {
        if (args != null && args.length > 0 && message.contains("%")) {
            try {
                return String.format(message, args);
            } catch (Exception e) {
                return message;
            }
        }
        return message;
    }
}
