package top.harrylei.forum.api.model.vo;

import top.harrylei.forum.api.model.vo.constants.StatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用响应封装类，用于 Controller 层统一返回结构
 *
 * @param <T> 返回结果的数据类型 结构包含：状态信息 {@link Status} 与返回实体 {@code result} 通过静态工厂方法 {@code ok} 和 {@code fail} 进行快速构造
 */
@Data
public class ResVO<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = -510306209659393854L;
    /**
     * 返回状态信息，包括状态码与提示信息
     */
    @Schema(description = "返回结果说明", requiredMode = Schema.RequiredMode.REQUIRED)
    private Status status;

    /**
     * 实际返回的数据实体，类型由调用方指定
     */
    @Schema(description = "返回的实体结果", requiredMode = Schema.RequiredMode.REQUIRED)
    private T result;

    /**
     * 默认构造函数，通常用于反序列化或框架调用
     */
    public ResVO() {}

    /**
     * 使用指定状态创建响应对象
     *
     * @param status 返回状态
     */
    public ResVO(Status status) {
        this.status = status;
    }

    /**
     * 构造成功状态的响应对象，并设置返回结果
     *
     * @param t 返回的业务数据
     */
    public ResVO(T t) {
        status = Status.newStatus(StatusEnum.SUCCESS);
        this.result = t;
    }

    /**
     * 快速构建成功响应
     *
     * @param t 返回数据
     * @param <T> 数据类型
     * @return 成功响应对象
     */
    public static <T> ResVO<T> ok(T t) {
        return new ResVO<>(t);
    }

    /**
     * 默认成功响应的返回信息
     */
    private static final String OK_DEFAULT_MESSAGE = "ok";

    /**
     * 构建默认成功响应，返回 "ok"
     *
     * @return 成功响应对象，返回值为字符串 "ok"
     */
    public static ResVO<String> ok() {
        return ok(OK_DEFAULT_MESSAGE);
    }

    /**
     * 构建失败响应，可传入参数进行状态信息格式化
     *
     * @param status 状态枚举
     * @param args 状态提示信息的参数
     * @param <T> 返回数据类型（通常为 null）
     * @return 失败响应对象
     */
    public static <T> ResVO<T> fail(StatusEnum status, Object... args) {
        return new ResVO<>(Status.newStatus(status, args));
    }

    /**
     * 使用指定状态信息构造失败响应
     *
     * @param status 状态信息
     * @param <T> 返回数据类型（通常为 null）
     * @return 失败响应对象
     */
    public static <T> ResVO<T> fail(Status status) {
        return new ResVO<>(status);
    }
}
