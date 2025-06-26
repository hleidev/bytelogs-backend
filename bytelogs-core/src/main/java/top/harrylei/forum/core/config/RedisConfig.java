package top.harrylei.forum.core.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import top.harrylei.forum.core.util.JsonUtil;

/**
 * Redis配置类 提供自定义的RedisTemplate配置，优化序列化方式
 *
 * @author harry
 */
@Configuration
public class RedisConfig {

    /**
     * 自定义RedisTemplate配置 使用String作为key的序列化器，GenericJackson2JsonRedisSerializer作为value的序列化器 适用于大部分场景，提供更好的可读性和兼容性
     *
     * @param redisConnectionFactory Redis连接工厂
     * @return 配置好的RedisTemplate实例
     */
    @Bean
    @ConditionalOnMissingBean(RedisTemplate.class)
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // 设置key的序列化器为StringRedisSerializer
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // 设置value的序列化器
        RedisSerializer<Object> jsonSerializer = new RedisSerializer<>() {
            @Override
            public byte[] serialize(Object obj) {
                return JsonUtil.toBytes(obj);
            }

            @Override
            public Object deserialize(byte[] bytes) {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                // 对于Redis，我们存储为字符串，反序列化时返回字符串
                // 具体的类型转换由业务层调用JsonUtil.parseObject处理
                return new String(bytes);
            }
        };
        
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }


    /**
     * 提供StringRedisTemplate的Bean 当只需要处理String类型的数据时，使用此模板更为高效
     *
     * @param redisConnectionFactory Redis连接工厂
     * @return 配置好的StringRedisTemplate实例
     */
    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
}
