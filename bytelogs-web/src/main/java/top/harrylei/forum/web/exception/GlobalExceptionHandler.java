package top.harrylei.forum.web.exception;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.StatusEnum;
import top.harrylei.forum.api.model.vo.ResVO;
import top.harrylei.forum.core.exception.ForumAdviceException;
import top.harrylei.forum.core.exception.ForumException;

/**
 * 全局异常处理器
 * <p>
 * 用于集中处理系统中出现的各类异常，返回统一格式的错误响应
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常 ForumException
     * <p>
     * 业务异常通常由业务代码主动抛出，表示一个可预期的业务错误
     */
    @ExceptionHandler(ForumException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResVO<Void> handleForumException(ForumException e, HttpServletRequest request) {
        log.warn("业务异常：{}, 请求路径：{}", e.getMessage(), request.getRequestURI());
        return ResVO.fail(e.getStatusEnum().getCode(), e.getMessage());
    }

    /**
     * 处理业务通知类异常 ForumAdviceException
     * <p>
     * 区别于普通业务异常，通知类异常仅用于告知前端特定的业务状态，不记录错误日志
     */
    @ExceptionHandler(ForumAdviceException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResVO<Void> handleForumAdviceException(ForumAdviceException e) {
        return ResVO.fail(e.getStatusEnum().getCode(), e.getMessage());
    }

    /**
     * 当没有管理员权限时返回合适的错误信息
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResVO<Void> handleAccessDeniedException() {
        return ResVO.fail(StatusEnum.FORBID_ERROR_MIXED, "当前用户无管理员权限");
    }

    /**
     * 处理参数校验异常（Bean Validation 注解校验）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResVO<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
        HttpServletRequest request) {
        String message = buildBindingResultErrorMessage(e.getBindingResult());
        log.warn("参数校验失败：{}, 请求路径：{}", message, request.getRequestURI());
        return ResVO.fail(StatusEnum.PARAM_VALIDATE_FAILED, message);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResVO<Void> handleBindException(BindException e, HttpServletRequest request) {
        String message = buildBindingResultErrorMessage(e.getBindingResult());
        log.warn("参数绑定失败：{}, 请求路径：{}", message, request.getRequestURI());
        return ResVO.fail(StatusEnum.PARAM_ERROR, message);
    }

    /**
     * 处理约束违反异常（路径参数校验）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResVO<Void> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String message = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("; "));
        log.warn("约束校验失败：{}, 请求路径：{}", message, request.getRequestURI());
        return ResVO.fail(StatusEnum.PARAM_VALIDATE_FAILED, message);
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResVO<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e,
        HttpServletRequest request) {
        String message = String.format("缺少必需的请求参数: %s", e.getParameterName());
        log.warn("缺少请求参数：{}, 请求路径：{}", message, request.getRequestURI());
        return ResVO.fail(StatusEnum.PARAM_MISSING, e.getParameterName());
    }

    /**
     * 处理请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResVO<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e,
        HttpServletRequest request) {
        String message = String.format("不支持的请求方法: %s", e.getMethod());
        log.warn("请求方法不支持：{}, 请求路径：{}", message, request.getRequestURI());
        return ResVO.fail(StatusEnum.METHOD_NOT_ALLOWED);
    }

    /**
     * 处理请求体解析失败异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResVO<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e,
        HttpServletRequest request) {
        log.warn("请求体解析失败：{}, 请求路径：{}", e.getMessage(), request.getRequestURI());
        return ResVO.fail(StatusEnum.PARAM_ERROR, "请求体格式错误或解析失败");
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResVO<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e,
        HttpServletRequest request) {
        String type = e.getRequiredType() == null ? "未知" : e.getRequiredType().getSimpleName();
        String message = String.format("参数类型不匹配, 参数 '%s' 应为 %s 类型", e.getName(), type);
        log.warn("参数类型不匹配：{}, 请求路径：{}", message, request.getRequestURI());
        return ResVO.fail(StatusEnum.PARAM_TYPE_ERROR, message);
    }

    /**
     * 处理数据库访问异常
     */
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResVO<Void> handleDataAccessException(DataAccessException e, HttpServletRequest request) {
        log.error("数据库访问异常, 请求路径: {}", request.getRequestURI(), e);
        return ResVO.fail(StatusEnum.SYSTEM_ERROR, "数据库操作异常");
    }

    /**
     * 处理所有未明确处理的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResVO<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("未处理的异常, 请求路径: {}", request.getRequestURI(), e);
        return ResVO.fail(StatusEnum.SYSTEM_ERROR, "服务器内部错误");
    }

    /**
     * 构建绑定错误消息
     */
    private String buildBindingResultErrorMessage(BindingResult bindingResult) {
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        if (fieldErrors.isEmpty()) {
            return "参数错误";
        }

        return fieldErrors.stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining("; "));
    }
}