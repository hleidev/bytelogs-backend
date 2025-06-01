package top.harrylei.forum.api.model.vo;

import top.harrylei.forum.api.model.vo.constants.StatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用状态返回对象
 *
 * 封装接口返回中的状态码与提示信息，配合 StatusEnum 使用，用于构造统一响应体结构。 通常作为 ResVO<T>; 的嵌套字段，用于标识接口是否执行成功及描述信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "返回状态对象")
public class Status {

    /**
     * 业务状态码 0 表示成功，非 0 表示异常（由 StatusEnum 定义）
     */
    @Schema(description = "状态码, 0表示成功返回，其他异常返回", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private int code;

    /**
     * 描述信息 成功时为 "ok"，失败时为错误提示信息（支持占位符填充）
     */
    @Schema(description = "正确返回时为ok，异常时为描述文案", requiredMode = Schema.RequiredMode.REQUIRED, example = "ok")
    private String msg;

    /**
     * 创建自定义状态对象
     *
     * @param code 状态码
     * @param msg 描述信息
     * @return Status 实例
     */
    public static Status newStatus(int code, String msg) {
        return new Status(code, msg);
    }

    /**
     * 根据状态枚举和可变参数创建状态对象
     *
     * 支持占位符格式化，例如： StatusEnum.USER_NOT_FOUND = "用户不存在: %s" 调用：newStatus(USER_NOT_FOUND, "harry") → "用户不存在: harry"
     *
     * @param status 状态枚举
     * @param msgs 用于填充状态信息中的占位符
     * @return Status 实例
     */
    public static Status newStatus(StatusEnum status, Object... msgs) {
        String msg;
        if (msgs.length > 0) {
            msg = String.format(status.getMessage(), msgs);
        } else {
            msg = status.getMessage();
        }
        return newStatus(status.getCode(), msg);
    }
}
