package top.harrylei.forum.web.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.harrylei.forum.api.enums.ResultCode;
import top.harrylei.forum.api.enums.user.UserRoleEnum;
import top.harrylei.forum.api.model.auth.AuthReq;
import top.harrylei.forum.api.model.base.ResVO;
import top.harrylei.forum.api.model.user.dto.UserInfoDetailDTO;
import top.harrylei.forum.api.model.user.vo.UserInfoVO;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.security.permission.RequiresAdmin;
import top.harrylei.forum.service.auth.service.AuthService;
import top.harrylei.forum.service.user.converted.UserStructMapper;

/**
 * 管理员认证模块
 *
 * @author harry
 */
@Tag(name = "管理员认证模块", description = "提供登录、退出等接口")
@Slf4j
@RestController
@RequestMapping("/v1/admin/auth")
@RequiredArgsConstructor
@Validated
public class AuthManagementController {

    private final AuthService authService;
    private final UserStructMapper userStructMapper;

    /**
     * 管理员登录接口
     *
     * @param authReq  登录请求体，包含用户名和密码
     * @param response 响应对象，用于设置Token
     * @return 登录结果
     */
    @Operation(summary = "登录账号", description = "校验管理员密码，成功后返回JWT令牌")
    @PostMapping("/login")
    public ResVO<UserInfoVO> login(@Valid @RequestBody AuthReq authReq, HttpServletResponse response) {
        String token = authService.login(authReq.getUsername(),
                                         authReq.getPassword(),
                                         authReq.getKeepLogin(),
                                         UserRoleEnum.ADMIN);

        response.setHeader("Authorization", "Bearer " + token);
        response.setHeader("Access-Control-Expose-Headers", "Authorization");

        // 从请求上下文获取已认证的用户信息
        UserInfoDetailDTO userInfo = ReqInfoContext.getContext().getUser();
        if (userInfo == null) {
            ResultCode.USER_NOT_EXISTS.throwException();
        }

        return ResVO.ok(userStructMapper.toVO(userInfo));
    }

    /**
     * 管理员退出接口
     *
     * @return 退出结果
     */
    @Operation(summary = "退出登录", description = "通过JWT令牌注销当前登录状态")
    @RequiresAdmin
    @PostMapping("/logout")
    public ResVO<Void> logout() {
        Long userId = ReqInfoContext.getContext().getUserId();
        authService.logout(userId);
        return ResVO.ok();
    }
}
