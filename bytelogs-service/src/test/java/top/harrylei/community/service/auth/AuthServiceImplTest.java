package top.harrylei.community.service.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import top.harrylei.community.api.enums.response.ResultCode;
import top.harrylei.community.api.enums.user.LoginTypeEnum;
import top.harrylei.community.api.enums.user.UserRoleEnum;
import top.harrylei.community.api.enums.user.UserStatusEnum;
import top.harrylei.community.api.exception.BusinessException;
import top.harrylei.community.api.model.user.dto.UserInfoDTO;
import top.harrylei.community.core.context.ReqInfoContext;
import top.harrylei.community.core.util.BCryptUtil;
import top.harrylei.community.core.util.JwtUtil;
import top.harrylei.community.core.util.RedisUtil;
import top.harrylei.community.service.auth.service.impl.AuthServiceImpl;
import top.harrylei.community.service.user.repository.dao.UserDAO;
import top.harrylei.community.service.user.repository.dao.UserInfoDAO;
import top.harrylei.community.service.user.repository.entity.UserDO;
import top.harrylei.community.service.user.service.cache.UserCacheService;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 认证服务测试
 *
 * @author harry
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl 测试")
class AuthServiceImplTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private UserInfoDAO userInfoDAO;

    @Mock
    private RedisUtil redisUtil;

    @Mock
    private UserCacheService userCacheService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private MockedStatic<BCryptUtil> bcryptMock;
    private MockedStatic<ReqInfoContext> contextMock;

    @BeforeEach
    void setUp() {
        bcryptMock = mockStatic(BCryptUtil.class);
        contextMock = mockStatic(ReqInfoContext.class);

        // 默认上下文设置
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setUserId(1L);
        contextMock.when(ReqInfoContext::getContext).thenReturn(reqInfo);
    }

    @AfterEach
    void tearDown() {
        if (bcryptMock != null) {
            bcryptMock.close();
        }
        if (contextMock != null) {
            contextMock.close();
        }
    }

    @Nested
    @DisplayName("register 方法测试")
    class RegisterTest {

        @Test
        @DisplayName("有效参数应注册成功")
        void shouldRegisterSuccessfully() {
            // Given
            String username = "newuser";
            String password = "Password1";
            UserRoleEnum role = UserRoleEnum.NORMAL;

            when(userDAO.getUserByUsername(username)).thenReturn(null);
            bcryptMock.when(() -> BCryptUtil.encode(password)).thenReturn("encodedPassword");

            // When
            authService.register(username, password, role);

            // Then
            verify(userDAO).getUserByUsername(username);
            verify(userDAO).save(any(UserDO.class));
            verify(userInfoDAO).save(any());
        }

        @Test
        @DisplayName("用户名已存在应抛出异常")
        void shouldThrowExceptionWhenUsernameExists() {
            // Given
            String username = "existinguser";
            String password = "Password1";
            UserDO existingUser = new UserDO();
            existingUser.setId(1L);

            when(userDAO.getUserByUsername(username)).thenReturn(existingUser);

            // When & Then
            assertThatThrownBy(() -> authService.register(username, password, UserRoleEnum.NORMAL))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(ResultCode.USER_ALREADY_EXISTS.getCode());
                    });
        }

        @Test
        @DisplayName("无效密码应抛出异常")
        void shouldThrowExceptionForInvalidPassword() {
            // Given
            String username = "newuser";
            String password = "weak"; // 太短，不符合规则

            // When & Then
            assertThatThrownBy(() -> authService.register(username, password, UserRoleEnum.NORMAL))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(ResultCode.AUTH_PASSWORD_INVALID.getCode());
                    });
        }

        @Test
        @DisplayName("非管理员创建管理员账号应抛出异常")
        void shouldThrowExceptionWhenNonAdminCreatesAdmin() {
            // Given
            String username = "newadmin";
            String password = "Password1";

            when(userDAO.getUserByUsername(username)).thenReturn(null);

            // 设置非管理员上下文
            ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
            reqInfo.setUserId(1L);
            // 不设置 ADMIN 权限
            contextMock.when(ReqInfoContext::getContext).thenReturn(reqInfo);

            // When & Then
            assertThatThrownBy(() -> authService.register(username, password, UserRoleEnum.ADMIN))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(ResultCode.FORBIDDEN.getCode());
                    });
        }
    }

    @Nested
    @DisplayName("login 方法测试")
    class LoginTest {

        @Test
        @DisplayName("有效凭证应登录成功")
        void shouldLoginSuccessfully() {
            // Given
            String username = "testuser";
            String password = "Password1";
            Long userId = 1L;
            String expectedToken = "jwt.token.here";

            UserDO user = new UserDO();
            user.setId(userId);
            user.setUserName(username);
            user.setPassword("encodedPassword");
            user.setStatus(UserStatusEnum.ENABLED);

            UserInfoDTO userInfo = new UserInfoDTO();
            userInfo.setUserId(userId);
            userInfo.setUserRole(UserRoleEnum.NORMAL);

            when(userDAO.getUserByUsername(username)).thenReturn(user);
            bcryptMock.when(() -> BCryptUtil.notMatches(password, "encodedPassword")).thenReturn(false);
            when(userCacheService.getUserInfo(userId)).thenReturn(userInfo);
            when(jwtUtil.generateToken(userId, UserRoleEnum.NORMAL, false)).thenReturn(expectedToken);
            when(jwtUtil.getDefaultExpire()).thenReturn(Duration.ofHours(2));

            // When
            String token = authService.login(username, password, false);

            // Then
            assertThat(token).isEqualTo(expectedToken);
            verify(redisUtil).set(anyString(), eq(expectedToken), any(Duration.class));
        }

        @Test
        @DisplayName("用户不存在应抛出异常")
        void shouldThrowExceptionWhenUserNotExists() {
            // Given
            String username = "nonexistent";
            String password = "Password1";

            when(userDAO.getUserByUsername(username)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> authService.login(username, password, false))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(ResultCode.AUTH_LOGIN_FAILED.getCode());
                    });
        }

        @Test
        @DisplayName("密码错误应抛出异常")
        void shouldThrowExceptionForWrongPassword() {
            // Given
            String username = "testuser";
            String password = "wrongPassword1";

            UserDO user = new UserDO();
            user.setId(1L);
            user.setUserName(username);
            user.setPassword("encodedPassword");
            user.setStatus(UserStatusEnum.ENABLED);

            when(userDAO.getUserByUsername(username)).thenReturn(user);
            bcryptMock.when(() -> BCryptUtil.notMatches(password, "encodedPassword")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.login(username, password, false))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(ResultCode.AUTH_LOGIN_FAILED.getCode());
                    });
        }

        @Test
        @DisplayName("账号被禁用应抛出异常")
        void shouldThrowExceptionWhenUserDisabled() {
            // Given
            String username = "disableduser";
            String password = "Password1";

            UserDO user = new UserDO();
            user.setId(1L);
            user.setUserName(username);
            user.setPassword("encodedPassword");
            user.setStatus(UserStatusEnum.DISABLED);

            when(userDAO.getUserByUsername(username)).thenReturn(user);

            // When & Then
            assertThatThrownBy(() -> authService.login(username, password, false))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(ResultCode.USER_DISABLED.getCode());
                    });
        }

        @Test
        @DisplayName("keepLogin=true 应使用更长的过期时间")
        void shouldUseKeepLoginExpireWhenKeepLoginTrue() {
            // Given
            String username = "testuser";
            String password = "Password1";
            Long userId = 1L;

            UserDO user = new UserDO();
            user.setId(userId);
            user.setUserName(username);
            user.setPassword("encodedPassword");
            user.setStatus(UserStatusEnum.ENABLED);

            UserInfoDTO userInfo = new UserInfoDTO();
            userInfo.setUserId(userId);
            userInfo.setUserRole(UserRoleEnum.NORMAL);

            when(userDAO.getUserByUsername(username)).thenReturn(user);
            bcryptMock.when(() -> BCryptUtil.notMatches(password, "encodedPassword")).thenReturn(false);
            when(userCacheService.getUserInfo(userId)).thenReturn(userInfo);
            when(jwtUtil.generateToken(userId, UserRoleEnum.NORMAL, true)).thenReturn("token");
            when(jwtUtil.getKeepLoginExpire()).thenReturn(Duration.ofDays(7));

            // When
            authService.login(username, password, true);

            // Then
            verify(jwtUtil).getKeepLoginExpire();
            verify(redisUtil).set(anyString(), anyString(), eq(Duration.ofDays(7)));
        }

        @Test
        @DisplayName("管理员登录验证角色失败应抛出异常")
        void shouldThrowExceptionWhenAdminLoginWithNormalUser() {
            // Given
            String username = "normaluser";
            String password = "Password1";
            Long userId = 1L;

            UserDO user = new UserDO();
            user.setId(userId);
            user.setUserName(username);
            user.setPassword("encodedPassword");
            user.setStatus(UserStatusEnum.ENABLED);

            UserInfoDTO userInfo = new UserInfoDTO();
            userInfo.setUserId(userId);
            userInfo.setUserRole(UserRoleEnum.NORMAL); // 普通用户

            when(userDAO.getUserByUsername(username)).thenReturn(user);
            bcryptMock.when(() -> BCryptUtil.notMatches(password, "encodedPassword")).thenReturn(false);
            when(userCacheService.getUserInfo(userId)).thenReturn(userInfo);

            // When & Then
            assertThatThrownBy(() -> authService.login(username, password, false, UserRoleEnum.ADMIN))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(ResultCode.FORBIDDEN.getCode());
                    });
        }
    }

    @Nested
    @DisplayName("logout 方法测试")
    class LogoutTest {

        @Test
        @DisplayName("有效用户 ID 应登出成功")
        void shouldLogoutSuccessfully() {
            // Given
            Long userId = 1L;
            when(redisUtil.del(anyString())).thenReturn(true);

            // When
            authService.logout(userId);

            // Then
            verify(redisUtil).del(anyString());
            verify(userCacheService).clearUserInfoCache(userId);
        }

        @Test
        @DisplayName("null 用户 ID 应抛出异常")
        void shouldThrowExceptionForNullUserId() {
            // When & Then
            assertThatThrownBy(() -> authService.logout(null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(ResultCode.INVALID_PARAMETER.getCode());
                    });
        }

        @Test
        @DisplayName("Redis 删除失败应不抛出异常")
        void shouldNotThrowExceptionWhenRedisDeleteFails() {
            // Given
            Long userId = 1L;
            when(redisUtil.del(anyString())).thenReturn(false);

            // When & Then (should not throw)
            authService.logout(userId);

            verify(redisUtil).del(anyString());
        }
    }
}
