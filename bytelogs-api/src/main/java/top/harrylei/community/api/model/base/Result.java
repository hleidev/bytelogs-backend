package top.harrylei.community.api.model.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.harrylei.community.api.enums.response.ResultCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用响应封装类
 *
 * @param <T> 返回结果的数据类型
 * @author harry
 */
@Data
@Schema(description = "统一响应结构")
public class Result<T> implements Serializable {
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
    @Schema(description = "响应消息，成功时为'success'，失败时为具体错误信息", requiredMode = Schema.RequiredMode.REQUIRED, example = "success")
    private String message;

    /**
     * 业务数据
     */
    @Schema(description = "业务数据，失败时可能为null")
    private T data;

    /**
     * 默认构造函数
     */
    public Result() {
    }

    /**
     * 完整构造函数
     */
    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 构造成功响应，仅包含数据
     */
    public Result(T data) {
        this.code = ResultCode.SUCCESS.getCode();
        this.message = ResultCode.SUCCESS.getMessage();
        this.data = data;
    }

    /**
     * 构造成功响应
     *
     * @param data 业务数据
     * @param <T>  数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(data);
    }

    /**
     * 构建默认成功响应
     *
     * @return 成功响应，无数据
     */
    public static Result<Void> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 构建失败响应
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }


    /**
     * 构建失败响应
     *
     * @param status 状态枚举
     * @param args   参数列表
     * @return 失败响应
     */
    public static <T> Result<T> fail(ResultCode status, Object... args) {
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
