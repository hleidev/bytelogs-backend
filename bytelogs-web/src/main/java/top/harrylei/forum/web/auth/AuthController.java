package top.harrylei.forum.web.auth;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.vo.ResVO;
import top.harrylei.forum.api.model.vo.auth.AuthReq;
import top.harrylei.forum.api.model.vo.constants.StatusEnum;
import top.harrylei.forum.service.auth.service.AuthService;

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

    /**
     * 用户注册接口
     *
     * @param authReq 注册请求体，包含用户名和密码
     * @return 注册结果
     */
    @Operation(summary = "用户注册", description = "通过用户名和密码进行注册")
    @PostMapping("/register")
    public ResVO<Boolean> register(@Valid @RequestBody AuthReq authReq) {
        String username = authReq.getUsername();
        String password = authReq.getPassword();

        try {
            if (authService.register(username, password)) {
                log.info("用户注册成功: username={}", username);
                return ResVO.ok(true);
            } else {
                return ResVO.fail(StatusEnum.LOGIN_FAILED_MIXED, "注册失败，请稍后重试");
            }
        } catch (Exception e) {
            log.error("用户注册异常: username={}, 原因: {}", username, e.getMessage(), e);
            return ResVO.fail(StatusEnum.LOGIN_FAILED_MIXED, e.getMessage());
        }
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
    public ResVO<Boolean> login(@Valid @RequestBody AuthReq authReq, HttpServletResponse response) {
        String username = authReq.getUsername();
        String password = authReq.getPassword();

        try {
            String token = authService.login(username, password);
            if (StringUtils.isNotBlank(token)) {
                // 将JWT令牌添加到响应头
                response.setHeader("Authorization", "Bearer " + token);
                response.setHeader("Access-Control-Expose-Headers", "Authorization");
                log.info("用户登录成功: username={}", username);
                return ResVO.ok(true);
            } else {
                return ResVO.fail(StatusEnum.LOGIN_FAILED_MIXED, "用户名或密码登录错误，请稍后重试");
            }
        } catch (Exception e) {
            log.error("用户登录异常: username={}, 原因: {}", username, e.getMessage(), e);
            return ResVO.fail(StatusEnum.LOGIN_FAILED_MIXED, e.getMessage());
        }
    }

    /**
     * 用户注销接口
     *
     * @param request HTTP请求对象，用于获取JWT Token
     * @return 注销结果
     */
    @Operation(summary = "用户注销", description = "通过JWT令牌注销当前登录状态")
    @PostMapping("/logout")
    public ResVO<Boolean> logout(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            // 处理Bearer前缀
            if (StringUtils.isNotBlank(token) && token.startsWith("Bearer ")) {
                token = token.substring(7);
                authService.logout(token);
                log.info("用户注销成功");
            }
            return ResVO.ok(true);
        } catch (Exception e) {
            log.error("用户注销异常, 原因: {}", e.getMessage(), e);
            return ResVO.fail(StatusEnum.FORBID_NOTLOGIN, e.getMessage());
        }
    }
}
