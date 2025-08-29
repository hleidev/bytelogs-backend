package top.harrylei.community.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import top.harrylei.community.api.enums.response.ResultCode;
import top.harrylei.community.api.model.base.ResVO;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * 自定义访问拒绝处理器，处理已认证但权限不足的请求
 *
 * @author harry
 */
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        // 设置响应状态码和内容类型
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 创建标准错误响应
        ResVO<Void> errorResponse = ResVO.fail(ResultCode.FORBIDDEN.getCode(), "权限不足，无法访问该资源");

        // 或者如果想保持Spring Boot的错误格式，可以用下面的代码
        // Map<String, Object> errorResponse = new HashMap<>();
        // errorResponse.put("timestamp", dateFormat.format(new Date()));
        // errorResponse.put("status", HttpStatus.FORBIDDEN.value());
        // errorResponse.put("error", "Forbidden");
        // errorResponse.put("message", "权限不足，无法访问该资源");
        // errorResponse.put("path", request.getRequestURI());

        // 写入响应
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(errorResponse));
        writer.flush();
    }
}