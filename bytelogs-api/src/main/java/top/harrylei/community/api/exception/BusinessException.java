package top.harrylei.community.api.exception;

import lombok.Getter;

/**
 * 业务异常类
 *
 * @author harry
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 构造方法
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("BusinessException[%d]: %s", code, message);
    }
}