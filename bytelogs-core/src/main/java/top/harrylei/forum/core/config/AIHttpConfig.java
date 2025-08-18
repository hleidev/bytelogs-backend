package top.harrylei.forum.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * AI客户端HTTP配置
 *
 * @author harry
 */
@Configuration
public class AIHttpConfig {

    /**
     * 共享的HttpClient实例
     */
    @Bean
    public HttpClient aiHttpClient(AIConfig aiConfig) {
        AIConfig.HttpConfig httpConfig = aiConfig.getHttp();
        
        return HttpClient.newBuilder()
                .connectTimeout(httpConfig.getConnectTimeout())
                .build();
    }
}