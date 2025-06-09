package top.harrylei.forum.api.model.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 后台新建用户请求参数
 */
@Data
@Schema(description = "后台新建用户请求参数")
public class UserCreateReq {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{4,16}$", message = "用户名只能包含字母、数字、下划线和连字符，长度为4-16位")
    @Schema(description = "用户名", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_@#%&!$*-]{8,20}$", message = "密码必须包含字母、数字，可包含特殊字符，长度为8~20位")
    @Schema(description = "初始密码", example = "admin@123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    /**
     * 角色编码
     */
    @NotNull(message = "角色不能为空")
    @Schema(description = "角色编码", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer role;
}