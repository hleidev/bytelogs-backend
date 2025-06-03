package top.harrylei.forum.api.model.vo.user.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户信息更新请求
 */
@Data
@Schema(description = "用户信息更新请求")
@Accessors(chain = true)
public class UserInfoReq {
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{4,16}$", message = "用户名只能包含字母、数字、下划线和连字符，长度为4-16位")
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userName;

    /**
     * 个人简介
     */
    @Schema(description = "用户简介")
    private String profile;
    /**
     * 职位
     */
    @Schema(description = "个人职位")
    private String position;

    /**
     * 公司
     */
    @Schema(description = "公司")
    private String company;
}
