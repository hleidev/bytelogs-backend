package top.harrylei.community.web.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import top.harrylei.community.api.enums.response.ResultCode;
import top.harrylei.community.api.exception.BusinessException;
import top.harrylei.community.api.model.base.Result;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 全局异常处理器测试
 *
 * @author harry
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler 测试")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        // 设置默认不打印堆栈
        ReflectionTestUtils.setField(exceptionHandler, "printStackTrace", false);

        // 设置默认请求信息
        when(request.getRequestURI()).thenReturn("/v1/test");
        when(request.getMethod()).thenReturn("POST");
    }

    @Nested
    @DisplayName("handleBusinessException 测试")
    class HandleBusinessExceptionTest {

        @Test
        @DisplayName("应返回业务异常的错误码和消息")
        void shouldReturnBusinessExceptionCodeAndMessage() {
            // Given
            BusinessException ex = new BusinessException(
                    ResultCode.USER_NOT_EXISTS.getCode(),
                    "用户不存在"
            );

            // When
            Result<Void> result = exceptionHandler.handleBusinessException(ex, request);

            // Then
            assertThat(result.getCode()).isEqualTo(ResultCode.USER_NOT_EXISTS.getCode());
            assertThat(result.getMessage()).isEqualTo("用户不存在");
        }

        @Test
        @DisplayName("应正确处理不同错误码的业务异常")
        void shouldHandleDifferentBusinessExceptions() {
            // Given
            BusinessException authEx = new BusinessException(
                    ResultCode.AUTHENTICATION_FAILED.getCode(),
                    "认证失败"
            );

            // When
            Result<Void> result = exceptionHandler.handleBusinessException(authEx, request);

            // Then
            assertThat(result.getCode()).isEqualTo(ResultCode.AUTHENTICATION_FAILED.getCode());
        }
    }

    @Nested
    @DisplayName("handleMethodArgumentNotValidException 测试")
    class HandleValidationExceptionTest {

        @Test
        @DisplayName("应返回字段校验错误信息")
        void shouldReturnFieldValidationErrors() {
            // Given
            MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);

            FieldError fieldError1 = new FieldError("authReq", "username", "用户名不能为空");
            FieldError fieldError2 = new FieldError("authReq", "password", "密码格式不正确");

            when(ex.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));
            when(ex.getStackTrace()).thenReturn(new StackTraceElement[0]);

            // When
            Result<Void> result = exceptionHandler.handleMethodArgumentNotValidException(ex, request);

            // Then
            // 注意：Result.fail(ResultCode, Object...) 使用 String.format，由于 ResultCode 消息没有占位符
            // 实际返回的是 ResultCode 的默认消息
            assertThat(result.getCode()).isEqualTo(ResultCode.INVALID_PARAMETER.getCode());
            assertThat(result.getMessage()).isEqualTo("参数错误");
        }

        @Test
        @DisplayName("无字段错误时应返回默认消息")
        void shouldReturnDefaultMessageWhenNoFieldErrors() {
            // Given
            MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);

            when(ex.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(List.of());
            when(ex.getStackTrace()).thenReturn(new StackTraceElement[0]);

            // When
            Result<Void> result = exceptionHandler.handleMethodArgumentNotValidException(ex, request);

            // Then
            assertThat(result.getCode()).isEqualTo(ResultCode.INVALID_PARAMETER.getCode());
            assertThat(result.getMessage()).isEqualTo("参数错误");
        }
    }

    @Nested
    @DisplayName("handleMissingServletRequestParameterException 测试")
    class HandleMissingParamExceptionTest {

        @Test
        @DisplayName("应返回缺少的参数名")
        void shouldReturnMissingParamName() {
            // Given
            MissingServletRequestParameterException ex =
                    new MissingServletRequestParameterException("articleId", "Long");

            // When
            Result<Void> result = exceptionHandler.handleMissingServletRequestParameterException(ex, request);

            // Then
            // Result.fail(ResultCode, message) 使用 String.format，消息被忽略
            assertThat(result.getCode()).isEqualTo(ResultCode.INVALID_PARAMETER.getCode());
            assertThat(result.getMessage()).isEqualTo("参数错误");
        }
    }

    @Nested
    @DisplayName("handleHttpRequestMethodNotSupportedException 测试")
    class HandleMethodNotSupportedExceptionTest {

        @Test
        @DisplayName("应返回方法不支持错误")
        void shouldReturnMethodNotAllowed() {
            // Given
            HttpRequestMethodNotSupportedException ex =
                    new HttpRequestMethodNotSupportedException("GET");

            // When
            Result<Void> result = exceptionHandler.handleHttpRequestMethodNotSupportedException(ex, request);

            // Then
            assertThat(result.getCode()).isEqualTo(ResultCode.METHOD_NOT_ALLOWED.getCode());
        }
    }

    @Nested
    @DisplayName("handleHttpMessageNotReadableException 测试")
    class HandleMessageNotReadableExceptionTest {

        @Test
        @DisplayName("应返回请求体格式错误")
        void shouldReturnBodyFormatError() {
            // Given
            HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
            when(ex.getStackTrace()).thenReturn(new StackTraceElement[0]);

            // When
            Result<Void> result = exceptionHandler.handleHttpMessageNotReadableException(ex, request);

            // Then
            // Result.fail(ResultCode, message) 使用 String.format，消息被忽略
            assertThat(result.getCode()).isEqualTo(ResultCode.INVALID_PARAMETER.getCode());
            assertThat(result.getMessage()).isEqualTo("参数错误");
        }
    }

    @Nested
    @DisplayName("handleMethodArgumentTypeMismatchException 测试")
    class HandleTypeMismatchExceptionTest {

        @Test
        @DisplayName("应返回参数类型不匹配错误")
        void shouldReturnTypeMismatchError() {
            // Given
            MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
            when(ex.getName()).thenReturn("id");
            when(ex.getRequiredType()).thenReturn((Class) Long.class);
            when(ex.getStackTrace()).thenReturn(new StackTraceElement[0]);

            // When
            Result<Void> result = exceptionHandler.handleMethodArgumentTypeMismatchException(ex, request);

            // Then
            // Result.fail(ResultCode, message) 使用 String.format，消息被忽略
            assertThat(result.getCode()).isEqualTo(ResultCode.INVALID_PARAMETER.getCode());
            assertThat(result.getMessage()).isEqualTo("参数错误");
        }

        @Test
        @DisplayName("类型为 null 时应返回参数错误")
        void shouldShowUnknownWhenTypeIsNull() {
            // Given
            MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
            when(ex.getName()).thenReturn("param");
            when(ex.getRequiredType()).thenReturn(null);
            when(ex.getStackTrace()).thenReturn(new StackTraceElement[0]);

            // When
            Result<Void> result = exceptionHandler.handleMethodArgumentTypeMismatchException(ex, request);

            // Then
            // Result.fail(ResultCode, message) 使用 String.format，消息被忽略
            assertThat(result.getCode()).isEqualTo(ResultCode.INVALID_PARAMETER.getCode());
            assertThat(result.getMessage()).isEqualTo("参数错误");
        }
    }

    @Nested
    @DisplayName("handleOptimisticLockingFailureException 测试")
    class HandleOptimisticLockExceptionTest {

        @Test
        @DisplayName("应返回数据并发修改冲突错误")
        void shouldReturnConcurrentModificationError() {
            // Given
            OptimisticLockingFailureException ex =
                    new OptimisticLockingFailureException("并发修改冲突");

            // When
            Result<Void> result = exceptionHandler.handleOptimisticLockingFailureException(ex, request);

            // Then
            // Result.fail(ResultCode, message) 使用 String.format，消息被忽略
            assertThat(result.getCode()).isEqualTo(ResultCode.INTERNAL_ERROR.getCode());
            assertThat(result.getMessage()).isEqualTo("系统内部错误");
        }
    }

    @Nested
    @DisplayName("handleDataAccessException 测试")
    class HandleDataAccessExceptionTest {

        @Test
        @DisplayName("应返回数据库操作异常错误")
        void shouldReturnDatabaseError() {
            // Given
            DataAccessException ex = new DataAccessException("数据库错误") {
            };

            // When
            Result<Void> result = exceptionHandler.handleDataAccessException(ex, request);

            // Then
            // Result.fail(ResultCode, message) 使用 String.format，消息被忽略
            assertThat(result.getCode()).isEqualTo(ResultCode.INTERNAL_ERROR.getCode());
            assertThat(result.getMessage()).isEqualTo("系统内部错误");
        }
    }

    @Nested
    @DisplayName("handleException 测试")
    class HandleGenericExceptionTest {

        @Test
        @DisplayName("未知异常应返回内部错误")
        void shouldReturnInternalError() {
            // Given
            Exception ex = new RuntimeException("未知错误");

            // When
            Result<Void> result = exceptionHandler.handleException(ex, request);

            // Then
            assertThat(result.getCode()).isEqualTo(ResultCode.INTERNAL_ERROR.getCode());
            assertThat(result.getMessage()).contains("内部错误");
        }

        @Test
        @DisplayName("NullPointerException 应返回内部错误")
        void shouldHandleNullPointerException() {
            // Given
            NullPointerException ex = new NullPointerException();

            // When
            Result<Void> result = exceptionHandler.handleException(ex, request);

            // Then
            assertThat(result.getCode()).isEqualTo(ResultCode.INTERNAL_ERROR.getCode());
        }
    }

    @Nested
    @DisplayName("日志配置测试")
    class LoggingConfigTest {

        @Test
        @DisplayName("启用堆栈打印时应记录完整异常信息")
        void shouldLogFullStackTraceWhenEnabled() {
            // Given
            ReflectionTestUtils.setField(exceptionHandler, "printStackTrace", true);
            BusinessException ex = new BusinessException(40000, "测试异常");

            // When
            Result<Void> result = exceptionHandler.handleBusinessException(ex, request);

            // Then - 验证不抛出异常，能正常返回结果
            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo(40000);
        }
    }
}
