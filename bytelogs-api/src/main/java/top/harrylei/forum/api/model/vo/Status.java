package top.harrylei.forum.api.model.vo;

import top.harrylei.forum.api.model.vo.constants.StatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author YiHui
 * @date 2022/7/6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "返回状态对象")
public class Status {

    /**
     * 业务状态码
     */
    @Schema(description = "状态码, 0表示成功返回，其他异常返回", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private int code;

    /**
     * 描述信息
     */
    @Schema(description = "正确返回时为ok，异常时为描述文案", requiredMode = Schema.RequiredMode.REQUIRED, example = "ok")
    private String msg;

    public static Status newStatus(int code, String msg) {
        return new Status(code, msg);
    }

    public static Status newStatus(StatusEnum status, Object... msgs) {
        String msg;
        if (msgs.length > 0) {
            msg = String.format(status.getMsg(), msgs);
        } else {
            msg = status.getMsg();
        }
        return newStatus(status.getCode(), msg);
    }
}
