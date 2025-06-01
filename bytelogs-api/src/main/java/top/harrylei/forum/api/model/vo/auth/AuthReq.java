package top.harrylei.forum.api.model.vo.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 认证请求
 * <p>
 * 用于登录和注册接口的请求参数
 */
@Data
@Schema(description = "认证请求")
public class AuthReq {
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{4,16}$", message = "用户名只能包含字母、数字、下划线和连字符，长度为4-16位")
    @Schema(description = "用户名", example = "user123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度必须在8-20之间")
    @Schema(description = "密码", example = "Pass@123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
} 