package top.harrylei.community.core.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import top.harrylei.community.api.enums.user.UserRoleEnum;
import top.harrylei.community.core.config.JwtProperties;

import java.time.Duration;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

/**
 * JWT 工具类测试
 *
 * @author harry
 */
@DisplayName("JwtUtil 测试")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setIssuer("bytelogs-test");
        // 密钥必须至少 32 字节（256 位）以支持 HS256 算法
        jwtProperties.setSecret("bytelogs-test-secret-key-must-be-at-least-32-bytes-long");
        jwtProperties.setDefaultExpire(Duration.ofHours(2));
        jwtProperties.setKeepLoginExpire(Duration.ofDays(7));

        jwtUtil = new JwtUtil(jwtProperties);
    }

    @Nested
    @DisplayName("generateToken 方法测试")
    class GenerateTokenTest {

        @Test
        @DisplayName("正常参数应生成有效 token")
        void shouldGenerateValidToken() {
            String token = jwtUtil.generateToken(1L, UserRoleEnum.NORMAL, false);

            assertThat(token).isNotBlank();
            assertThat(token.split("\\.")).hasSize(3); // JWT 格式：header.payload.signature
        }

        @Test
        @DisplayName("管理员角色应生成有效 token")
        void shouldGenerateValidTokenForAdmin() {
            String token = jwtUtil.generateToken(1L, UserRoleEnum.ADMIN, false);

            assertThat(token).isNotBlank();
        }

        @Test
        @DisplayName("keepLogin=true 应生成更长有效期的 token")
        void shouldGenerateLongerTokenForKeepLogin() {
            String normalToken = jwtUtil.generateToken(1L, UserRoleEnum.NORMAL, false);
            String keepLoginToken = jwtUtil.generateToken(1L, UserRoleEnum.NORMAL, true);

            Date normalExpiration = jwtUtil.getTokenExpiration(normalToken);
            Date keepLoginExpiration = jwtUtil.getTokenExpiration(keepLoginToken);

            assertThat(keepLoginExpiration).isAfter(normalExpiration);
        }

        @Test
        @DisplayName("userId 为 null 应抛出异常")
        void shouldThrowExceptionForNullUserId() {
            assertThatThrownBy(() -> jwtUtil.generateToken(null, UserRoleEnum.NORMAL, false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("用户ID和角色不能为空");
        }

        @Test
        @DisplayName("role 为 null 应抛出异常")
        void shouldThrowExceptionForNullRole() {
            assertThatThrownBy(() -> jwtUtil.generateToken(1L, null, false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("用户ID和角色不能为空");
        }
    }

    @Nested
    @DisplayName("extractUserId 方法测试")
    class ExtractUserIdTest {

        @Test
        @DisplayName("有效 token 应返回正确的用户 ID")
        void shouldExtractUserIdFromValidToken() {
            Long expectedUserId = 12345L;
            String token = jwtUtil.generateToken(expectedUserId, UserRoleEnum.NORMAL, false);

            Long userId = jwtUtil.extractUserId(token);

            assertThat(userId).isEqualTo(expectedUserId);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("空或 null token 应返回 null")
        void shouldReturnNullForBlankToken(String token) {
            Long userId = jwtUtil.extractUserId(token);

            assertThat(userId).isNull();
        }

        @Test
        @DisplayName("无效 token 应返回 null")
        void shouldReturnNullForInvalidToken() {
            Long userId = jwtUtil.extractUserId("invalid.token.here");

            assertThat(userId).isNull();
        }

        @Test
        @DisplayName("篡改后的 token 应返回 null")
        void shouldReturnNullForTamperedToken() {
            String token = jwtUtil.generateToken(1L, UserRoleEnum.NORMAL, false);
            String tamperedToken = token.substring(0, token.length() - 5) + "xxxxx";

            Long userId = jwtUtil.extractUserId(tamperedToken);

            assertThat(userId).isNull();
        }
    }

    @Nested
    @DisplayName("extractUserRole 方法测试")
    class ExtractUserRoleTest {

        @Test
        @DisplayName("有效 token 应返回正确的用户角色（普通用户）")
        void shouldExtractNormalRoleFromValidToken() {
            String token = jwtUtil.generateToken(1L, UserRoleEnum.NORMAL, false);

            UserRoleEnum role = jwtUtil.extractUserRole(token);

            assertThat(role).isEqualTo(UserRoleEnum.NORMAL);
        }

        @Test
        @DisplayName("有效 token 应返回正确的用户角色（管理员）")
        void shouldExtractAdminRoleFromValidToken() {
            String token = jwtUtil.generateToken(1L, UserRoleEnum.ADMIN, false);

            UserRoleEnum role = jwtUtil.extractUserRole(token);

            assertThat(role).isEqualTo(UserRoleEnum.ADMIN);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("空或 null token 应返回 null")
        void shouldReturnNullForBlankToken(String token) {
            UserRoleEnum role = jwtUtil.extractUserRole(token);

            assertThat(role).isNull();
        }

        @Test
        @DisplayName("无效 token 应返回 null")
        void shouldReturnNullForInvalidToken() {
            UserRoleEnum role = jwtUtil.extractUserRole("invalid.token.here");

            assertThat(role).isNull();
        }
    }

    @Nested
    @DisplayName("isTokenExpired 方法测试")
    class IsTokenExpiredTest {

        @Test
        @DisplayName("新生成的 token 应未过期")
        void newTokenShouldNotBeExpired() {
            String token = jwtUtil.generateToken(1L, UserRoleEnum.NORMAL, false);

            boolean expired = jwtUtil.isTokenExpired(token);

            assertThat(expired).isFalse();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("空或 null token 应视为已过期")
        void blankTokenShouldBeExpired(String token) {
            boolean expired = jwtUtil.isTokenExpired(token);

            assertThat(expired).isTrue();
        }

        @Test
        @DisplayName("无效 token 应视为已过期")
        void invalidTokenShouldBeExpired() {
            boolean expired = jwtUtil.isTokenExpired("invalid.token.here");

            assertThat(expired).isTrue();
        }
    }

    @Nested
    @DisplayName("getTokenExpiration 方法测试")
    class GetTokenExpirationTest {

        @Test
        @DisplayName("有效 token 应返回未来的过期时间")
        void validTokenShouldHaveFutureExpiration() {
            String token = jwtUtil.generateToken(1L, UserRoleEnum.NORMAL, false);

            Date expiration = jwtUtil.getTokenExpiration(token);

            assertThat(expiration).isAfter(new Date());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("空或 null token 应返回过去的时间")
        void blankTokenShouldReturnPastDate(String token) {
            Date expiration = jwtUtil.getTokenExpiration(token);

            assertThat(expiration).isBefore(new Date());
        }
    }

    @Nested
    @DisplayName("getDefaultExpire 和 getKeepLoginExpire 方法测试")
    class ExpireConfigTest {

        @Test
        @DisplayName("getDefaultExpire 应返回配置的默认过期时间")
        void shouldReturnConfiguredDefaultExpire() {
            Duration defaultExpire = jwtUtil.getDefaultExpire();

            assertThat(defaultExpire).isEqualTo(Duration.ofHours(2));
        }

        @Test
        @DisplayName("getKeepLoginExpire 应返回配置的保持登录过期时间")
        void shouldReturnConfiguredKeepLoginExpire() {
            Duration keepLoginExpire = jwtUtil.getKeepLoginExpire();

            assertThat(keepLoginExpire).isEqualTo(Duration.ofDays(7));
        }
    }

    @Nested
    @DisplayName("集成测试")
    class IntegrationTest {

        @Test
        @DisplayName("完整的 token 生命周期测试")
        void fullTokenLifecycleTest() {
            // 1. 生成 token
            Long userId = 999L;
            UserRoleEnum role = UserRoleEnum.ADMIN;
            String token = jwtUtil.generateToken(userId, role, true);

            // 2. 验证 token 格式
            assertThat(token).isNotBlank();
            assertThat(token.split("\\.")).hasSize(3);

            // 3. 提取并验证用户信息
            assertThat(jwtUtil.extractUserId(token)).isEqualTo(userId);
            assertThat(jwtUtil.extractUserRole(token)).isEqualTo(role);

            // 4. 验证未过期
            assertThat(jwtUtil.isTokenExpired(token)).isFalse();

            // 5. 验证过期时间在未来
            Date expiration = jwtUtil.getTokenExpiration(token);
            assertThat(expiration).isAfter(new Date());
        }
    }
}
