package top.harrylei.community.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 密码工具类测试
 *
 * @author harry
 */
@DisplayName("PasswordUtil 测试")
class PasswordUtilTest {

    @Nested
    @DisplayName("isValid 方法测试")
    class IsValidTest {

        @ParameterizedTest
        @ValueSource(strings = {
                "Password1",        // 最小长度 8，字母+数字
                "Password12",       // 字母+数字
                "Password123456789", // 接近最大长度
                "Pass12345678901234", // 20 位（最大长度）
                "abc12345",         // 小写字母+数字
                "ABC12345",         // 大写字母+数字
                "Aa123456",         // 大小写字母+数字
                "Password1@",       // 包含特殊字符 @
                "Password1#",       // 包含特殊字符 #
                "Password1%",       // 包含特殊字符 %
                "Password1&",       // 包含特殊字符 &
                "Password1!",       // 包含特殊字符 !
                "Password1$",       // 包含特殊字符 $
                "Password1*",       // 包含特殊字符 *
                "Password1-",       // 包含特殊字符 -
                "Password1_"        // 包含特殊字符 _
        })
        @DisplayName("有效密码应返回 true")
        void validPasswordShouldReturnTrue(String password) {
            assertThat(PasswordUtil.isValid(password))
                    .as("Password '%s' should be valid", password)
                    .isTrue();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("null 或空字符串应返回 false")
        void nullOrEmptyShouldReturnFalse(String password) {
            assertThat(PasswordUtil.isValid(password)).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "Pass123",          // 太短（7 位）
                "Passwor",          // 太短（7 位，无数字）
                "Pass12345678901234abc", // 太长（21 位）
                "12345678",         // 纯数字
                "abcdefgh",         // 纯小写字母
                "ABCDEFGH",         // 纯大写字母
                "Password",         // 无数字
                "Password 1",       // 包含空格
                "Password1中文",    // 包含中文
                "Password1^",       // 包含不允许的特殊字符
                "Password1(",       // 包含不允许的特殊字符
                "Password1)",       // 包含不允许的特殊字符
                "Password1<",       // 包含不允许的特殊字符
                "Password1>"        // 包含不允许的特殊字符
        })
        @DisplayName("无效密码应返回 false")
        void invalidPasswordShouldReturnFalse(String password) {
            assertThat(PasswordUtil.isValid(password))
                    .as("Password '%s' should be invalid", password)
                    .isFalse();
        }

        @Test
        @DisplayName("正好 8 位的有效密码应通过")
        void exactlyMinLengthShouldPass() {
            assertThat(PasswordUtil.isValid("Pass1234")).isTrue();
        }

        @Test
        @DisplayName("正好 20 位的有效密码应通过")
        void exactlyMaxLengthShouldPass() {
            assertThat(PasswordUtil.isValid("Pass12345678901234ab")).isTrue();
        }

        @Test
        @DisplayName("7 位密码应不通过")
        void sevenCharsShouldFail() {
            assertThat(PasswordUtil.isValid("Pass123")).isFalse();
        }

        @Test
        @DisplayName("21 位密码应不通过")
        void twentyOneCharsShouldFail() {
            assertThat(PasswordUtil.isValid("Pass123456789012345ab")).isFalse();
        }
    }

    @Nested
    @DisplayName("isInvalid 方法测试")
    class IsInvalidTest {

        @Test
        @DisplayName("有效密码应返回 false")
        void validPasswordShouldReturnFalse() {
            assertThat(PasswordUtil.isInvalid("Password1")).isFalse();
        }

        @Test
        @DisplayName("无效密码应返回 true")
        void invalidPasswordShouldReturnTrue() {
            assertThat(PasswordUtil.isInvalid("invalid")).isTrue();
        }

        @Test
        @DisplayName("null 应返回 true")
        void nullShouldReturnTrue() {
            assertThat(PasswordUtil.isInvalid(null)).isTrue();
        }

        @Test
        @DisplayName("空字符串应返回 true")
        void emptyShouldReturnTrue() {
            assertThat(PasswordUtil.isInvalid("")).isTrue();
        }

        @Test
        @DisplayName("isValid 和 isInvalid 应互为反义")
        void isValidAndIsInvalidShouldBeOpposite() {
            String validPassword = "Password1";
            String invalidPassword = "123";

            assertThat(PasswordUtil.isValid(validPassword)).isTrue();
            assertThat(PasswordUtil.isInvalid(validPassword)).isFalse();

            assertThat(PasswordUtil.isValid(invalidPassword)).isFalse();
            assertThat(PasswordUtil.isInvalid(invalidPassword)).isTrue();
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTest {

        @Test
        @DisplayName("只有空格的密码应返回 false")
        void onlySpacesShouldReturnFalse() {
            assertThat(PasswordUtil.isValid("        ")).isFalse();
        }

        @Test
        @DisplayName("混合大小写字母和数字的密码应返回 true")
        void mixedCaseWithNumbersShouldPass() {
            assertThat(PasswordUtil.isValid("aAbBcC12")).isTrue();
        }

        @Test
        @DisplayName("所有允许特殊字符组合的密码应通过")
        void allAllowedSpecialCharsShouldPass() {
            assertThat(PasswordUtil.isValid("Pass1@#%&!$*-_")).isTrue();
        }
    }
}
