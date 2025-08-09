package top.harrylei.forum.web.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
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
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import top.harrylei.forum.api.enums.ErrorCodeEnum;
import top.harrylei.forum.api.exception.BusinessException;
import top.harrylei.forum.api.model.base.ResVO;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.exception.ForumException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author harry
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常 BusinessException
     */
    @ExceptionHandler(BusinessException.class)
    public ResVO<Void> handleBusinessException(BusinessException e,
                                               HttpServletRequest request,
                                               HttpServletResponse response) {
        logBusinessException("业务异常", e.getMessage(), request);
        // 设置对应的HTTP状态码
        response.setStatus(e.getHttpStatus());
        return ResVO.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理业务异常 ForumException (向后兼容)
     */
    @ExceptionHandler(ForumException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResVO<Void> handleForumException(ForumException e, HttpServletRequest request) {
        logBusinessException("业务异常", e.getMessage(), request);
        return ResVO.fail(e.getErrorCodeEnum().getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（Bean Validation 注解校验）
     * 参数异常属于客户端错误，使用WARN级别，不记录堆栈
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResVO<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
                                                             HttpServletRequest request) {
        String message = buildBindingResultErrorMessage(e.getBindingResult());
        logBusinessException("参数校验失败", message, request);
        return ResVO.fail(ErrorCodeEnum.PARAM_VALIDATE_FAILED, message);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResVO<Void> handleBindException(BindException e, HttpServletRequest request) {
        String message = buildBindingResultErrorMessage(e.getBindingResult());
        logBusinessException("参数绑定失败", message, request);
        return ResVO.fail(ErrorCodeEnum.PARAM_ERROR, message);
    }

    /**
     * 处理约束违反异常（路径参数校验）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResVO<Void> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String message = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("; "));
        logBusinessException("约束校验失败", message, request);
        return ResVO.fail(ErrorCodeEnum.PARAM_VALIDATE_FAILED, message);
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResVO<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e,
                                                                     HttpServletRequest request) {
        String message = String.format("缺少必需的请求参数: %s", e.getParameterName());
        logBusinessException("缺少请求参数", message, request);
        return ResVO.fail(ErrorCodeEnum.PARAM_MISSING, e.getParameterName());
    }

    /**
     * 处理权限不足异常
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResVO<Void> handleAuthDenied(AuthorizationDeniedException e, HttpServletRequest req) {
        logBusinessException("权限不足", "访问被拒绝", req);
        return ResVO.fail(ErrorCodeEnum.FORBIDDEN, "权限不足");
    }

    /**
     * 处理请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResVO<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e,
                                                                    HttpServletRequest request) {
        String message = String.format("不支持的请求方法: %s", e.getMethod());
        logBusinessException("请求方法不支持", message, request);
        return ResVO.fail(ErrorCodeEnum.METHOD_NOT_ALLOWED);
    }

    /**
     * 处理请求体解析失败异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResVO<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e,
                                                             HttpServletRequest request) {
        logBusinessException("请求体解析失败", "JSON格式错误或请求体为空", request);
        return ResVO.fail(ErrorCodeEnum.PARAM_ERROR, "请求体格式错误或解析失败");
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
        logBusinessException("参数类型不匹配", message, request);
        return ResVO.fail(ErrorCodeEnum.PARAM_TYPE_ERROR, message);
    }

    /**
     * 处理JSON处理异常
     */
    @ExceptionHandler(JsonProcessingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResVO<Void> handleJsonProcessingException(JsonProcessingException e, HttpServletRequest request) {
        logBusinessException("JSON处理异常", "JSON格式错误", request);
        return ResVO.fail(ErrorCodeEnum.PARAM_ERROR, "JSON格式错误");
    }

    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResVO<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e,
                                                            HttpServletRequest request) {
        logBusinessException("文件上传大小超限", "上传文件过大", request);
        return ResVO.fail(ErrorCodeEnum.PARAM_ERROR, "上传文件大小超过限制");
    }

    /**
     * 处理乐观锁异常
     * 乐观锁冲突属于并发问题，使用WARN级别
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResVO<Void> handleOptimisticLockingFailureException(OptimisticLockingFailureException e,
                                                               HttpServletRequest request) {
        logBusinessException("乐观锁冲突", "数据并发修改冲突", request);
        return ResVO.fail(ErrorCodeEnum.SYSTEM_ERROR, "数据已被其他用户修改，请刷新后重试");
    }

    /**
     * 处理数据库访问异常
     * 数据库异常属于系统异常，使用ERROR级别，记录完整堆栈
     */
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResVO<Void> handleDataAccessException(DataAccessException e, HttpServletRequest request) {
        logSystemException("数据库访问异常", e, request);
        return ResVO.fail(ErrorCodeEnum.SYSTEM_ERROR, "数据库操作异常");
    }

    /**
     * 处理所有未明确处理的异常
     * 未知异常属于系统异常，使用ERROR级别，记录完整堆栈
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResVO<Void> handleException(Exception e, HttpServletRequest request) {
        logSystemException("未处理的异常", e, request);
        return ResVO.fail(ErrorCodeEnum.SYSTEM_ERROR, "服务器内部错误");
    }

    /**
     * 记录业务异常日志（WARN级别，不记录堆栈）
     * 格式: [异常类型] 错误信息 | 路径: {} | 用户: {} | 方法: {}
     */
    private void logBusinessException(String exceptionType, String message, HttpServletRequest request) {
        Long userId = getCurrentUserId();
        log.warn("[{}] {} | 路径: {} | 用户: {} | 方法: {}",
                 exceptionType, message, request.getRequestURI(), userId, request.getMethod());
    }

    /**
     * 记录系统异常日志（ERROR级别，包含完整堆栈）
     * 格式: [异常类型] 系统异常 | 路径: {} | 用户: {} | 方法: {} | 异常: {}
     */
    private void logSystemException(String exceptionType, Exception e, HttpServletRequest request) {
        Long userId = getCurrentUserId();
        log.error("[{}] 系统异常 | 路径: {} | 用户: {} | 方法: {} | 异常: {}",
                  exceptionType, request.getRequestURI(), userId, request.getMethod(), e.getClass().getSimpleName(), e);
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        try {
            return ReqInfoContext.getContext().getUserId();
        } catch (Exception e) {
            return null;
        }
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