package top.harrylei.forum.api.model.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.harrylei.forum.api.enums.ErrorCodeEnum;
import top.harrylei.forum.api.enums.ResultCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用响应封装类
 *
 * @param <T> 返回结果的数据类型
 */
@Data
@Schema(description = "统一响应结构")
public class ResVO<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = -510306209659393854L;

    /**
     * 业务状态码
     */
    @Schema(description = "业务状态码，0表示成功，其他表示异常", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private int code;

    /**
     * 响应消息
     */
    @Schema(description = "响应消息，成功时为'OK'，失败时为具体错误信息", requiredMode = Schema.RequiredMode.REQUIRED, example = "OK")
    private String message;

    /**
     * 业务数据
     */
    @Schema(description = "业务数据，失败时可能为null")
    private T data;

    /**
     * 默认构造函数
     */
    public ResVO() {
    }

    /**
     * 完整构造函数
     */
    public ResVO(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 构造成功响应，仅包含数据
     */
    public ResVO(T data) {
        this.code = 200;
        this.message = "OK";
        this.data = data;
    }

    /**
     * 构造成功响应
     *
     * @param data 业务数据
     * @param <T>  数据类型
     * @return 成功响应
     */
    public static <T> ResVO<T> ok(T data) {
        return new ResVO<>(data);
    }

    /**
     * 构建默认成功响应
     *
     * @return 成功响应，无数据
     */
    public static ResVO<Void> ok() {
        return new ResVO<>(200, "OK", null);
    }

    /**
     * 构建失败响应
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> ResVO<T> fail(int code, String message) {
        return new ResVO<>(code, message, null);
    }

    /**
     * 使用状态枚举构建失败响应
     *
     * @param status 状态枚举
     * @param args   消息格式化参数
     * @param <T>    数据类型
     * @return 失败响应
     */
    public static <T> ResVO<T> fail(ErrorCodeEnum status, Object... args) {
        String message = formatMessage(status.getMessage(), args);
        return fail(status.getCode(), message);
    }

    /**
     * 构建失败响应
     *
     * @param status 状态枚举
     * @param args   参数列表
     * @return 失败响应
     */
    public static <T> ResVO<T> fail(ResultCode status, Object... args) {
        String message = formatMessage(status.getMessage(), args);
        return fail(status.getCode(), message);
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
