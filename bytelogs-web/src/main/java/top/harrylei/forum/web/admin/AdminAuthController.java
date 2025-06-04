package top.harrylei.forum.web.admin;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.StatusEnum;
import top.harrylei.forum.api.model.enums.user.UserRoleEnum;
import top.harrylei.forum.api.model.vo.ResVO;
import top.harrylei.forum.api.model.vo.auth.AuthReq;
import top.harrylei.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import top.harrylei.forum.api.model.vo.user.req.PasswordUpdateReq;
import top.harrylei.forum.api.model.vo.user.vo.UserInfoVO;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.service.auth.service.AuthService;
import top.harrylei.forum.service.user.converted.UserInfoStructMapper;
import top.harrylei.forum.service.user.service.UserService;
import top.harrylei.forum.service.util.JwtUtil;
import top.harrylei.forum.web.security.permission.RequiresAdmin;

@Tag(name = "管理员认证模块", description = "提供登录、退出等接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
@Validated
public class AdminAuthController {

    private final AuthService authService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserInfoStructMapper userInfoStructMapper;

    /**
     * 管理员登录接口
     *
     * @param authReq 登录请求体，包含用户名和密码
     * @param response 响应对象，用于设置Token
     * @return 登录结果
     */
    @Operation(summary = "登录账号", description = "校验管理员密码，成功后返回JWT令牌")
    @PostMapping("/login")
    public ResVO<UserInfoVO> login(@Valid @RequestBody AuthReq authReq, HttpServletResponse response) {
        String token = authService.login(authReq.getUsername(), authReq.getPassword(), UserRoleEnum.ADMIN);
        ExceptionUtil.requireNonEmpty(token, StatusEnum.USER_LOGIN_FAILED, "token 为空");

        response.setHeader("Authorization", "Bearer " + token);
        response.setHeader("Access-Control-Expose-Headers", "Authorization");

        BaseUserInfoDTO userInfo = ReqInfoContext.getContext().getUser();
        ExceptionUtil.requireNonNull(userInfo, StatusEnum.USER_INFO_NOT_EXISTS);

        return ResVO.ok(userInfoStructMapper.toVO(userInfo));
    }

    /**
     * 管理员退出接口
     *
     * @param authHeader 获取请求中的token
     * @return 退出结果
     */
    @Operation(summary = "退出登录", description = "通过JWT令牌注销当前登录状态")
    @RequiresAdmin
    @PostMapping("/logout")
    public ResVO<Void> logout(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        String token = jwtUtil.extractTokenFromAuthorizationHeader(authHeader);
        Long userId = ReqInfoContext.getContext().getUserId();

        if (StringUtils.isBlank(token)) {
            log.warn("管理员退出失败 userId={} reason=缺少有效认证信息", userId);
            return ResVO.fail(StatusEnum.REQUEST_BODY_ERROR);
        } else {
            authService.logout(token);
            return ResVO.ok();
        }
    }

    /**
     * 获取管理员信息
     * 
     * @return 返回管理员信息
     */
    @Operation(summary = "查询管理员信息", description = "从请求上下文中获取管理员用户信息")
    @RequiresAdmin
    @GetMapping("/profile")
    public ResVO<UserInfoVO> getProfile() {
        BaseUserInfoDTO userInfo = ReqInfoContext.getContext().getUser();

        ExceptionUtil.requireNonNull(userInfo, StatusEnum.USER_INFO_NOT_EXISTS);
        return ResVO.ok(userInfoStructMapper.toVO(userInfo));
    }


    /**
     * 修改管理员密码
     *
     * @param passwordUpdateReq 用户密码更新请求
     * @return 操作成功响应
     */
    @Operation(summary = "修改管理员密码", description = "修改当前管理员的个人密码")
    @RequiresAdmin
    @PostMapping("/update-password")
    public ResVO<Void> updatePassword(@RequestHeader(name = "Authorization", required = false) String authHeader,
                                      @Valid @RequestBody PasswordUpdateReq passwordUpdateReq) {
        String token = jwtUtil.extractTokenFromAuthorizationHeader(authHeader);
        ExceptionUtil.requireNonEmpty(token, StatusEnum.USER_UPDATE_FAILED, "缺少有效的 Authorization 头");

        userService.updatePassword(token, passwordUpdateReq.getOldPassword(), passwordUpdateReq.getNewPassword());
        return ResVO.ok();
    }}
