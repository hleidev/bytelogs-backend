package top.harrylei.forum.web.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.harrylei.forum.core.common.converter.StringToLocalDateTimeConverter;

/**
 * Spring MVC 配置类
 *
 * @author harry
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 添加LocalDateTime转换器
        registry.addConverter(new StringToLocalDateTimeConverter());
    }

    /**
     * 配置ObjectMapper，支持LocalDateTime等Java 8时间类型
     * 项目中统一使用此配置，既适用于Web序列化也适用于Redis序列化
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }
}