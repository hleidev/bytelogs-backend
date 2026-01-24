package top.harrylei.community.api.enums.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import top.harrylei.community.api.exception.BusinessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 响应码枚举测试
 *
 * @author harry
 */
@DisplayName("ResultCode 测试")
class ResultCodeTest {

    @Nested
    @DisplayName("throwException 方法测试")
    class ThrowExceptionTest {

        @Test
        @DisplayName("无参数时应抛出原始消息异常")
        void shouldThrowExceptionWithOriginalMessage() {
            assertThatThrownBy(() -> ResultCode.USER_NOT_EXISTS.throwException())
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(42001);
                        assertThat(bex.getMessage()).isEqualTo("用户不存在");
                    });
        }

        @Test
        @DisplayName("有参数时应抛出带详细信息的异常")
        void shouldThrowExceptionWithDetailMessage() {
            assertThatThrownBy(() -> ResultCode.USER_NOT_EXISTS.throwException("testUser"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(42001);
                        assertThat(bex.getMessage()).isEqualTo("用户不存在: testUser");
                    });
        }

        @Test
        @DisplayName("多个参数时应全部拼接")
        void shouldThrowExceptionWithMultipleParams() {
            assertThatThrownBy(() -> ResultCode.INVALID_PARAMETER.throwException("param1", "param2"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(40000);
                        assertThat(bex.getMessage()).isEqualTo("参数错误: param1: param2");
                    });
        }

        @Test
        @DisplayName("SUCCESS 也能抛出异常")
        void successCanAlsoThrowException() {
            assertThatThrownBy(() -> ResultCode.SUCCESS.throwException())
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(0);
                        assertThat(bex.getMessage()).isEqualTo("success");
                    });
        }
    }

    @Nested
    @DisplayName("isSuccess 方法测试")
    class IsSuccessTest {

        @Test
        @DisplayName("SUCCESS 应返回 true")
        void successShouldReturnTrue() {
            assertThat(ResultCode.SUCCESS.isSuccess()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "INVALID_PARAMETER",
                "AUTHENTICATION_FAILED",
                "USER_NOT_EXISTS",
                "INTERNAL_ERROR"
        })
        @DisplayName("非 SUCCESS 应返回 false")
        void nonSuccessShouldReturnFalse(String enumName) {
            ResultCode code = ResultCode.valueOf(enumName);
            assertThat(code.isSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("getByCode 方法测试")
    class GetByCodeTest {

        @ParameterizedTest(name = "code={0} 应返回 {1}")
        @CsvSource({
                "0, SUCCESS",
                "40000, INVALID_PARAMETER",
                "40001, AUTHENTICATION_FAILED",
                "42001, USER_NOT_EXISTS",
                "50000, INTERNAL_ERROR"
        })
        @DisplayName("有效状态码应返回对应枚举")
        void shouldReturnEnumForValidCode(int code, String expectedName) {
            ResultCode result = ResultCode.getByCode(code);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo(expectedName);
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 99999, 12345})
        @DisplayName("无效状态码应返回 null")
        void shouldReturnNullForInvalidCode(int code) {
            ResultCode result = ResultCode.getByCode(code);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("错误码分类测试")
    class ErrorCodeCategoryTest {

        @Test
        @DisplayName("成功状态码应为 0")
        void successCodeShouldBeZero() {
            assertThat(ResultCode.SUCCESS.getCode()).isEqualTo(0);
        }

        @Test
        @DisplayName("通用错误码应以 4 开头")
        void commonErrorCodesShouldStartWith4() {
            assertThat(ResultCode.INVALID_PARAMETER.getCode()).isGreaterThanOrEqualTo(40000).isLessThan(50000);
            assertThat(ResultCode.AUTHENTICATION_FAILED.getCode()).isGreaterThanOrEqualTo(40000).isLessThan(50000);
            assertThat(ResultCode.FORBIDDEN.getCode()).isGreaterThanOrEqualTo(40000).isLessThan(50000);
        }

        @Test
        @DisplayName("用户模块错误码应在 42xxx 范围")
        void userModuleErrorCodesShouldBeIn42xxx() {
            assertThat(ResultCode.USER_NOT_EXISTS.getCode()).isGreaterThanOrEqualTo(42000).isLessThan(43000);
            assertThat(ResultCode.USER_ALREADY_EXISTS.getCode()).isGreaterThanOrEqualTo(42000).isLessThan(43000);
            assertThat(ResultCode.USER_DISABLED.getCode()).isGreaterThanOrEqualTo(42000).isLessThan(43000);
        }

        @Test
        @DisplayName("系统错误码应以 5 开头")
        void systemErrorCodesShouldStartWith5() {
            assertThat(ResultCode.INTERNAL_ERROR.getCode()).isGreaterThanOrEqualTo(50000);
            assertThat(ResultCode.SERVICE_UNAVAILABLE.getCode()).isGreaterThanOrEqualTo(50000);
            assertThat(ResultCode.DATABASE_ERROR.getCode()).isGreaterThanOrEqualTo(50000);
        }
    }

    @Nested
    @DisplayName("枚举完整性测试")
    class EnumIntegrityTest {

        @Test
        @DisplayName("所有枚举值应有唯一的 code")
        void allEnumsShouldHaveUniqueCode() {
            ResultCode[] values = ResultCode.values();

            long distinctCodeCount = java.util.Arrays.stream(values)
                    .map(ResultCode::getCode)
                    .distinct()
                    .count();

            assertThat(distinctCodeCount).isEqualTo(values.length);
        }

        @Test
        @DisplayName("所有枚举值应有非空 message")
        void allEnumsShouldHaveNonEmptyMessage() {
            for (ResultCode code : ResultCode.values()) {
                assertThat(code.getMessage())
                        .as("ResultCode %s should have non-empty message", code.name())
                        .isNotBlank();
            }
        }
    }
}
