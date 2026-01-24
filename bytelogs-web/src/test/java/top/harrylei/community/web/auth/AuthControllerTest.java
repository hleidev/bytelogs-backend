package top.harrylei.community.web.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import top.harrylei.community.api.enums.response.ResultCode;
import top.harrylei.community.api.enums.user.UserRoleEnum;
import top.harrylei.community.api.exception.BusinessException;
import top.harrylei.community.api.model.auth.AuthReq;
import top.harrylei.community.core.context.ReqInfoContext;
import top.harrylei.community.core.util.JwtUtil;
import top.harrylei.community.core.util.RedisUtil;
import top.harrylei.community.service.auth.service.AuthService;
import top.harrylei.community.service.user.service.cache.UserCacheService;
import top.harrylei.community.web.config.TestSecurityConfig;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 认证控制器测试
 *
 * @author harry
 */
@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
@DisplayName("AuthController 测试")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // JwtAuthenticationFilter 依赖的 Bean
    @MockBean
    private UserCacheService userCacheService;

    @MockBean
    private RedisUtil redisUtil;

    @MockBean
    private JwtUtil jwtUtil;

    @Nested
    @DisplayName("POST /v1/auth/register 测试")
    class RegisterTest {

        @Test
        @DisplayName("有效请求应注册成功")
        void shouldRegisterSuccessfully() throws Exception {
            // Given
            AuthReq authReq = new AuthReq();
            authReq.setUsername("testuser");
            authReq.setPassword("Password123");

            doNothing().when(authService).register(anyString(), anyString(), any(UserRoleEnum.class));

            // When & Then
            mockMvc.perform(post("/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.message").value("success"));

            verify(authService).register(eq("testuser"), eq("Password123"), eq(UserRoleEnum.NORMAL));
        }

        @Test
        @DisplayName("用户名已存在应返回错误")
        void shouldReturnErrorWhenUsernameExists() throws Exception {
            // Given
            AuthReq authReq = new AuthReq();
            authReq.setUsername("existinguser");
            authReq.setPassword("Password123");

            doThrow(new BusinessException(ResultCode.USER_ALREADY_EXISTS.getCode(), "用户已存在"))
                    .when(authService).register(anyString(), anyString(), any(UserRoleEnum.class));

            // When & Then
            mockMvc.perform(post("/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.USER_ALREADY_EXISTS.getCode()));
        }

        @Test
        @DisplayName("无效密码格式应返回参数错误")
        void shouldReturnErrorForInvalidPasswordFormat() throws Exception {
            // Given
            AuthReq authReq = new AuthReq();
            authReq.setUsername("testuser");
            authReq.setPassword("weak"); // 不符合密码规则

            // When & Then
            mockMvc.perform(post("/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.INVALID_PARAMETER.getCode()));
        }

        @Test
        @DisplayName("空用户名应返回参数错误")
        void shouldReturnErrorForEmptyUsername() throws Exception {
            // Given
            AuthReq authReq = new AuthReq();
            authReq.setUsername("");
            authReq.setPassword("Password123");

            // When & Then
            mockMvc.perform(post("/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.INVALID_PARAMETER.getCode()));
        }
    }

    @Nested
    @DisplayName("POST /v1/auth/login 测试")
    class LoginTest {

        @Test
        @DisplayName("有效凭证应登录成功并返回 Token")
        void shouldLoginSuccessfullyWithToken() throws Exception {
            // Given
            AuthReq authReq = new AuthReq();
            authReq.setUsername("testuser");
            authReq.setPassword("Password123");
            authReq.setKeepLogin(false);

            String expectedToken = "jwt.token.here";
            when(authService.login(anyString(), anyString(), anyBoolean())).thenReturn(expectedToken);

            // When & Then
            mockMvc.perform(post("/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(header().string("Authorization", "Bearer " + expectedToken));

            verify(authService).login("testuser", "Password123", false);
        }

        @Test
        @DisplayName("keepLogin=true 应正确传递")
        void shouldPassKeepLoginFlag() throws Exception {
            // Given
            AuthReq authReq = new AuthReq();
            authReq.setUsername("testuser");
            authReq.setPassword("Password123");
            authReq.setKeepLogin(true);

            when(authService.login(anyString(), anyString(), eq(true))).thenReturn("token");

            // When & Then
            mockMvc.perform(post("/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authReq)))
                    .andExpect(status().isOk());

            verify(authService).login("testuser", "Password123", true);
        }

        @Test
        @DisplayName("用户名或密码错误应返回认证失败")
        void shouldReturnAuthFailedForWrongCredentials() throws Exception {
            // Given
            AuthReq authReq = new AuthReq();
            authReq.setUsername("testuser");
            authReq.setPassword("wrongPassword1");

            doThrow(new BusinessException(ResultCode.AUTH_LOGIN_FAILED.getCode(), "用户名或密码错误"))
                    .when(authService).login(anyString(), anyString(), anyBoolean());

            // When & Then
            mockMvc.perform(post("/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.AUTH_LOGIN_FAILED.getCode()));
        }

        @Test
        @DisplayName("账号被禁用应返回用户禁用错误")
        void shouldReturnUserDisabledError() throws Exception {
            // Given
            AuthReq authReq = new AuthReq();
            authReq.setUsername("disableduser");
            authReq.setPassword("Password123");

            doThrow(new BusinessException(ResultCode.USER_DISABLED.getCode(), "用户已被禁用"))
                    .when(authService).login(anyString(), anyString(), anyBoolean());

            // When & Then
            mockMvc.perform(post("/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.USER_DISABLED.getCode()));
        }
    }

    @Nested
    @DisplayName("POST /v1/auth/logout 测试")
    class LogoutTest {

        @Test
        @DisplayName("登出接口应成功返回")
        void shouldLogoutSuccessfully() throws Exception {
            // Given
            // 注意：由于 ReqInfoContext 是线程本地变量，在 MockMvc 请求中无法设置
            // 因此 userId 会是 null，验证 logout 被调用即可
            doNothing().when(authService).logout(any());

            // When & Then
            mockMvc.perform(post("/v1/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));

            verify(authService).logout(any());
        }
    }

    @Nested
    @DisplayName("请求格式验证测试")
    class RequestValidationTest {

        @Test
        @DisplayName("空请求体应返回参数错误")
        void shouldReturnErrorForEmptyBody() throws Exception {
            mockMvc.perform(post("/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.INVALID_PARAMETER.getCode()));
        }

        @Test
        @DisplayName("无效 JSON 应返回参数错误")
        void shouldReturnErrorForInvalidJson() throws Exception {
            mockMvc.perform(post("/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.INVALID_PARAMETER.getCode()));
        }
    }
}
