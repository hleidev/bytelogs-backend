package top.harrylei.community.core.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis配置类
 *
 * @author harry
 */
@Configuration
public class RedisConfig {

    /**
     * 配置RedisTemplate，提供RedisTemplate<String, Object>类型
     *
     * @param connectionFactory Redis连接工厂
     * @return RedisTemplate实例
     */
    @Bean
    @ConditionalOnMissingBean(RedisTemplate.class)
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.afterPropertiesSet();
        return template;
    }
}
