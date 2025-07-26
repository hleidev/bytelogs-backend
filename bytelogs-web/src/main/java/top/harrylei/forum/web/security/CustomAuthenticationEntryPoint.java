package top.harrylei.forum.web.security;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import top.harrylei.forum.api.enums.ErrorCodeEnum;
import top.harrylei.forum.api.model.base.ResVO;

/**
 * 自定义认证入口点，处理未认证或无权限的请求
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException {
        // 设置响应状态码和内容类型
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 创建标准错误响应
        ResVO<Void> errorResponse = ResVO.fail(ErrorCodeEnum.FORBID_ERROR_MIXED, "权限不足，无法访问");

        // 或者如果想保持Spring Boot的错误格式，可以用下面的代码
        // Map<String, Object> errorResponse = new HashMap<>();
        // errorResponse.put("timestamp", dateFormat.format(new Date()));
        // errorResponse.put("status", HttpStatus.FORBIDDEN.value());
        // errorResponse.put("error", "Forbidden");
        // errorResponse.put("message", "权限不足，无法访问");
        // errorResponse.put("path", request.getRequestURI());

        // 写入响应
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(errorResponse));
        writer.flush();
    }
}