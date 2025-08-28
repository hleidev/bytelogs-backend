package top.harrylei.community.api.exception;

import lombok.Getter;
import lombok.Setter;
import top.harrylei.community.api.enums.ResultCode;

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
     * 原始ResultCode
     */
    @Setter
    private ResultCode resultCode;

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

    /**
     * 构造方法
     *
     * @param code    错误码
     * @param message 错误消息
     * @param cause   原始异常
     */
    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    /**
     * 通过ResultCode构造
     *
     * @param resultCode 结果码枚举
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.resultCode = resultCode;
    }

    /**
     * 通过ResultCode构造
     *
     * @param resultCode 结果码枚举
     * @param cause      原始异常
     */
    public BusinessException(ResultCode resultCode, Throwable cause) {
        super(resultCode.getMessage(), cause);
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.resultCode = resultCode;
    }

    /**
     * 获取HTTP状态码
     */
    public int getHttpStatus() {
        // 根据错误码推断HTTP状态码
        if (code >= 40000 && code < 50000) {
            if (code == 40001) {
                return 401;
            }
            if (code == 40003) {
                return 403;
            }
            if (code == 40004) {
                return 404;
            }
            return 400;
        } else if (code >= 50000) {
            return 500;
        }
        return 200;
    }

    @Override
    public String toString() {
        return String.format("BusinessException[%d]: %s", code, message);
    }
}