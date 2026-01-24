package top.harrylei.community.api.enums.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 用户角色枚举测试
 *
 * @author harry
 */
@DisplayName("UserRoleEnum 测试")
class UserRoleEnumTest {

    @Nested
    @DisplayName("fromCode 方法测试")
    class FromCodeTest {

        @ParameterizedTest(name = "code={0} 应返回 {1}")
        @CsvSource({
                "0, NORMAL",
                "1, ADMIN"
        })
        @DisplayName("有效角色码应返回对应枚举")
        void shouldReturnEnumForValidCode(Integer code, String expectedName) {
            UserRoleEnum result = UserRoleEnum.fromCode(code);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo(expectedName);
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("null 应返回 null")
        void shouldReturnNullForNullCode(Integer code) {
            UserRoleEnum result = UserRoleEnum.fromCode(code);

            assertThat(result).isNull();
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 2, 100, 999})
        @DisplayName("无效角色码应返回 null")
        void shouldReturnNullForInvalidCode(Integer code) {
            UserRoleEnum result = UserRoleEnum.fromCode(code);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("getCode 和 getLabel 方法测试")
    class GetterTest {

        @Test
        @DisplayName("NORMAL 的 code 应为 0")
        void normalShouldHaveCode0() {
            assertThat(UserRoleEnum.NORMAL.getCode()).isEqualTo(0);
        }

        @Test
        @DisplayName("ADMIN 的 code 应为 1")
        void adminShouldHaveCode1() {
            assertThat(UserRoleEnum.ADMIN.getCode()).isEqualTo(1);
        }

        @Test
        @DisplayName("NORMAL 的 label 应为 '普通用户'")
        void normalShouldHaveCorrectLabel() {
            assertThat(UserRoleEnum.NORMAL.getLabel()).isEqualTo("普通用户");
        }

        @Test
        @DisplayName("ADMIN 的 label 应为 '管理员'")
        void adminShouldHaveCorrectLabel() {
            assertThat(UserRoleEnum.ADMIN.getLabel()).isEqualTo("管理员");
        }
    }

    @Nested
    @DisplayName("枚举完整性测试")
    class EnumIntegrityTest {

        @Test
        @DisplayName("应有且仅有 2 个角色")
        void shouldHaveExactlyTwoRoles() {
            assertThat(UserRoleEnum.values()).hasSize(2);
        }

        @Test
        @DisplayName("所有枚举值应有唯一的 code")
        void allEnumsShouldHaveUniqueCode() {
            UserRoleEnum[] values = UserRoleEnum.values();

            long distinctCodeCount = java.util.Arrays.stream(values)
                    .map(UserRoleEnum::getCode)
                    .distinct()
                    .count();

            assertThat(distinctCodeCount).isEqualTo(values.length);
        }

        @Test
        @DisplayName("所有枚举值应有非空 label")
        void allEnumsShouldHaveNonEmptyLabel() {
            for (UserRoleEnum role : UserRoleEnum.values()) {
                assertThat(role.getLabel())
                        .as("Role %s should have non-empty label", role.name())
                        .isNotBlank();
            }
        }
    }
}
