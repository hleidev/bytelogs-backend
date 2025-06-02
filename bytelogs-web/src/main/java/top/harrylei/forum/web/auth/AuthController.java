package top.harrylei.forum.web.auth;

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
import top.harrylei.forum.api.model.vo.ResVO;
import top.harrylei.forum.api.model.vo.auth.AuthReq;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.service.auth.service.AuthService;
import top.harrylei.forum.service.util.JwtUtil;

/**
 * 用户认证控制器
 * <p>
 * 提供注册、登录、注销等认证相关接口
 */
@Tag(name = "用户认证模块", description = "提供注册、登录、注销等接口")
@Slf4j
@RestController
@RequestMapping(path = "api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册接口
     *
     * @param authReq 注册请求体，包含用户名和密码
     * @return 注册结果
     */
    @Operation(summary = "用户注册", description = "通过用户名和密码进行注册")
    @PostMapping("/register")
    public ResVO<Void> register(@Valid @RequestBody AuthReq authReq) {
        // 直接调用服务，让全局异常处理器处理可能的异常
        authService.register(authReq.getUsername(), authReq.getPassword());
        return ResVO.ok();
    }

    /**
     * 用户登录接口
     *
     * @param authReq 登录请求体，包含用户名和密码
     * @param response 响应对象，用于设置Token
     * @return 登录结果
     */
    @Operation(summary = "用户登录", description = "校验用户名密码，成功后返回JWT令牌")
    @PostMapping("/login")
    public ResVO<Void> login(@Valid @RequestBody AuthReq authReq, HttpServletResponse response) {
        // 调用登录服务
        String token = authService.login(authReq.getUsername(), authReq.getPassword());

        // 将JWT令牌添加到响应头
        if (StringUtils.isNotBlank(token)) {
            response.setHeader("Authorization", "Bearer " + token);
            response.setHeader("Access-Control-Expose-Headers", "Authorization");
            return ResVO.ok();
        }

        // 登录失败但未抛出异常的情况
        ExceptionUtil.error(StatusEnum.USER_LOGIN_FAILED, "登录失败，请稍后重试");
        return null; // 不会执行到这里，因为上面会抛出异常
    }

    /**
     * 用户注销接口
     *
     * @param authHeader 获取请求中的token
     * @return 注销结果
     */
    @Operation(summary = "用户注销", description = "通过JWT令牌注销当前登录状态")
    @PostMapping("/logout")
    public ResVO<Void> logout(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        String token = jwtUtil.extractTokenFromAuthorizationHeader(authHeader);
        Long userId = ReqInfoContext.getContext().getUserId();

        if (StringUtils.isNotBlank(token)) {
            authService.logout(token);
            log.info("用户注销成功: userId={}", userId);
        } else {
            log.warn("注销请求缺少有效的Authorization头");
        }

        return ResVO.ok();
    }
}
