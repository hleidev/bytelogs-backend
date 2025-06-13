package top.harrylei.forum.web.admin;

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
import top.harrylei.forum.api.model.vo.user.dto.UserInfoDetailDTO;
import top.harrylei.forum.api.model.vo.user.req.PasswordUpdateReq;
import top.harrylei.forum.api.model.vo.user.vo.UserInfoVO;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.core.security.permission.RequiresAdmin;
import top.harrylei.forum.service.auth.service.AuthService;
import top.harrylei.forum.service.user.converted.UserStructMapper;
import top.harrylei.forum.service.user.service.UserService;

@Tag(name = "管理员认证模块", description = "提供登录、退出等接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
@Validated
public class AuthManagementController {

    private final AuthService authService;
    private final UserService userService;
    private final UserStructMapper userStructMapper;

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

        UserInfoDetailDTO userInfo = ReqInfoContext.getContext().getUser();
        ExceptionUtil.requireNonNull(userInfo, StatusEnum.USER_INFO_NOT_EXISTS);

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
        ExceptionUtil.requireNonNull(userId, StatusEnum.PARAM_VALIDATE_FAILED, "用户ID为空");
        authService.logout(userId);
        return ResVO.ok();
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
        UserInfoDetailDTO userInfo = ReqInfoContext.getContext().getUser();

        ExceptionUtil.requireNonNull(userInfo, StatusEnum.USER_INFO_NOT_EXISTS);
        return ResVO.ok(userStructMapper.toVO(userInfo));
    }


    /**
     * 修改管理员密码
     *
     * @param passwordUpdateReq 用户密码更新请求
     * @return 操作成功响应
     */
    @Operation(summary = "修改管理员密码", description = "修改当前管理员的个人密码")
    @RequiresAdmin
    @PutMapping("/password")
    public ResVO<Void> updatePassword(@Valid @RequestBody PasswordUpdateReq passwordUpdateReq) {
        Long userId = ReqInfoContext.getContext().getUserId();
        ExceptionUtil.requireNonNull(userId, StatusEnum.PARAM_VALIDATE_FAILED, "用户ID为空");
        userService.updatePassword(userId, passwordUpdateReq.getOldPassword(), passwordUpdateReq.getNewPassword());
        return ResVO.ok();
    }}
