package top.harrylei.forum.api.model.exception;

import lombok.Getter;
import top.harrylei.forum.api.model.vo.constants.StatusEnum;

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
    private final StatusEnum statusEnum;
    
    /**
     * 错误消息格式化参数
     */
    private final Object[] args;

    public ForumException(StatusEnum statusEnum, Object... args) {
        super(formatMessage(statusEnum.getMessage(), args));
        this.statusEnum = statusEnum;
        this.args = args;
    }
    
    /**
     * 格式化错误消息
     */
    private static String formatMessage(String message, Object... args) {
        if (args != null && args.length > 0) {
            return String.format(message, args);
        }
        return message;
    }
}
