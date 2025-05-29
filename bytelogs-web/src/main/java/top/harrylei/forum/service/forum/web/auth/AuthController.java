package top.harrylei.forum.service.forum.web.auth;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.vo.ResVo;
import top.harrylei.forum.api.model.vo.constants.StatusEnum;
import top.harrylei.forum.api.model.vo.auth.LoginReq;
import top.harrylei.forum.service.auth.service.AuthService;

/**
 * 用户认证控制器
 */
@Slf4j
@RestController
@RequestMapping(path = "api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService loginService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResVo<Boolean> register(@Valid @RequestBody LoginReq logReq, HttpServletResponse response) {
        String username = logReq.getUsername();
        String password = logReq.getPassword();
        
        try {
            if (loginService.register(username, password)) {
                log.info("用户注册成功: {}", username);
                return ResVo.ok(true);
            } else {
                return ResVo.fail(StatusEnum.LOGIN_FAILED_MIXED, "注册失败，请稍后重试");
            }
        } catch (Exception e) {
            log.error("用户注册异常: {}, 原因: {}", username, e.getMessage(), e);
            return ResVo.fail(StatusEnum.LOGIN_FAILED_MIXED, e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResVo<Boolean> login(@Valid @RequestBody LoginReq request, HttpServletResponse response) {
        String username = request.getUsername();
        String password = request.getPassword();
        
        try {
            String token = loginService.login(username, password);
            if (StringUtils.isNotBlank(token)) {
                // 将JWT令牌添加到响应头
                response.setHeader("Authorization", "Bearer " + token);
                response.setHeader("Access-Control-Expose-Headers", "Authorization");
                log.info("用户登录成功: {}", username);
                return ResVo.ok(true);
            } else {
                return ResVo.fail(StatusEnum.LOGIN_FAILED_MIXED, "登录失败，请稍后重试");
            }
        } catch (Exception e) {
            log.error("用户登录异常: {}, 原因: {}", username, e.getMessage(), e);
            return ResVo.fail(StatusEnum.LOGIN_FAILED_MIXED, e.getMessage());
        }
    }
}
