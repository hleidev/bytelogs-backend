package top.harrylei.forum.core.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis工具类 封装RedisTemplate，提供常用的Redis操作方法，支持底层连接和高级API操作
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 检查参数是否为空
     *
     * @param args 待检查的参数
     * @throws IllegalArgumentException 如果有参数为空
     */
    public static void nullCheck(Object... args) {
        for (Object obj : args) {
            if (obj == null) {
                throw new IllegalArgumentException("redis argument can not be null!");
            }
        }
    }

    /**
     * 将字符串转换为字节数组
     *
     * @param key 字符串
     * @return 字节数组
     */
    private byte[] keyBytes(String key) {
        nullCheck(key);
        return key.getBytes(CHARSET);
    }

    /**
     * 将对象转换为字节数组
     *
     * @param <T> 对象类型
     * @param val 要序列化的对象
     * @return 字节数组，序列化失败返回null
     */
    private <T> byte[] valueBytes(T val) {
        if (val == null) {
            return null;
        }

        try {
            if (val instanceof String) {
                return ((String)val).getBytes(CHARSET);
            }
            @SuppressWarnings("unchecked")
            RedisSerializer<Object> serializer = (RedisSerializer<Object>)redisTemplate.getValueSerializer();
            return serializer.serialize(val);
        } catch (Exception e) {
            log.error("序列化对象失败: 对象类型={}, 错误信息={}", val.getClass().getName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将字节数组反序列化为字符串
     *
     * @param bytes 字节数组
     * @return 字符串，bytes为null时返回null
     */
    private String bytesToString(byte[] bytes) {
        return (bytes == null) ? null : new String(bytes, CHARSET);
    }

    /**
     * 将字节数组反序列化为对象
     *
     * @param <T> 对象类型
     * @param bytes 字节数组
     * @param clazz 对象类型
     * @return 反序列化后的对象，反序列化失败或bytes为null时返回null
     */
    @SuppressWarnings("unchecked")
    private <T> T bytesToObj(byte[] bytes, Class<T> clazz) {
        if (bytes == null) {
            return null;
        }

        if (String.class.equals(clazz)) {
            return (T)new String(bytes, CHARSET);
        }

        try {
            RedisSerializer<Object> serializer = (RedisSerializer<Object>)redisTemplate.getValueSerializer();
            return (T)serializer.deserialize(bytes);
        } catch (Exception e) {
            log.error("反序列化对象失败: 目标类型={}, 错误信息={}", clazz.getName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 设置对象值
     *
     * @param <T> 对象类型
     * @param key 键
     * @param value 值
     * @param seconds 过期时间（秒）
     * @return 是否成功，操作异常时返回false
     */
    public <T> Boolean setObj(String key, T value, long seconds) {
        try {
            return redisTemplate.execute((RedisCallback<Boolean>)con -> {
                byte[] keyBytes = keyBytes(key);
                byte[] valueBytes = valueBytes(value);
                if (valueBytes == null) {
                    log.warn("Redis设置对象失败: key={}, 对象序列化结果为null", key);
                    return false;
                }
                if (seconds > 0) {
                    return con.stringCommands().set(keyBytes, valueBytes, Expiration.seconds(seconds),
                        RedisStringCommands.SetOption.UPSERT);
                } else {
                    return con.stringCommands().set(keyBytes, valueBytes);
                }
            });
        } catch (Exception e) {
            log.error("Redis设置对象失败: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 设置对象值（不过期）
     *
     * @param <T> 对象类型
     * @param key 键
     * @param value 值
     * @return 是否成功，操作异常时返回false
     */
    public <T> Boolean setObj(String key, T value) {
        return setObj(key, value, 0);
    }

    /**
     * 获取对象值
     *
     * @param <T> 对象类型
     * @param key 键
     * @param clazz 对象类型
     * @return 对象，键不存在或操作异常时返回null
     */
    public <T> T getObj(String key, Class<T> clazz) {
        try {
            return redisTemplate.execute((RedisCallback<T>)con -> {
                byte[] val = con.stringCommands().get(keyBytes(key));
                return bytesToObj(val, clazz);
            });
        } catch (Exception e) {
            log.error("Redis获取对象失败: key={}, error={}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 删除键
     *
     * @param key 键
     * @return 是否成功，操作异常时返回false
     */
    public Boolean del(String key) {
        try {
            return redisTemplate.execute((RedisCallback<Boolean>)con -> {
                Long result = con.keyCommands().del(keyBytes(key));
                return result != null && result > 0;
            });
        } catch (Exception e) {
            log.error("Redis删除键失败: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 批量删除键
     *
     * @param keys 键集合
     * @return 删除的键数量，操作异常时返回0
     */
    public Long del(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }

        try {
            return redisTemplate.execute((RedisCallback<Long>)con -> {
                long count = 0;
                for (String key : keys) {
                    Long result = con.keyCommands().del(keyBytes(key));
                    if (result != null && result > 0) {
                        count++;
                    }
                }
                return count;
            });
        } catch (Exception e) {
            log.error("Redis批量删除键失败: keys={}, error={}", keys, e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * 判断键是否存在
     *
     * @param key 键
     * @return 是否存在，操作异常时返回false
     */
    public Boolean hasKey(String key) {
        try {
            return redisTemplate.execute((RedisCallback<Boolean>)con -> con.keyCommands().exists(keyBytes(key)));
        } catch (Exception e) {
            log.error("Redis检查键是否存在失败: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 设置过期时间
     *
     * @param key 键
     * @param seconds 过期时间（秒）
     * @return 是否成功，操作异常时返回false
     */
    public Boolean expire(String key, long seconds) {
        try {
            return redisTemplate
                .execute((RedisCallback<Boolean>)con -> con.keyCommands().expire(keyBytes(key), seconds));
        } catch (Exception e) {
            log.error("Redis设置过期时间失败: key={}, seconds={}, error={}", key, seconds, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取过期时间
     *
     * @param key 键
     * @return 过期时间（秒），-1表示永不过期，-2表示键不存在或操作异常
     */
    public Long getExpire(String key) {
        try {
            return redisTemplate.execute((RedisCallback<Long>)con -> con.keyCommands().ttl(keyBytes(key)));
        } catch (Exception e) {
            log.error("Redis获取过期时间失败: key={}, error={}", key, e.getMessage(), e);
            return -2L;
        }
    }

    /**
     * 自增/自减操作
     *
     * @param key 键
     * @param delta 增量（正值为自增，负值为自减）
     * @return 操作后的值，失败返回null
     */
    public Long incrBy(String key, long delta) {
        try {
            return redisTemplate.execute((RedisCallback<Long>)con -> con.stringCommands().incrBy(keyBytes(key), delta));
        } catch (Exception e) {
            log.error("Redis incrBy 操作失败: key={}, delta={}, error={}", key, delta, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 自增操作
     *
     * @param key 键
     * @return 操作后的值，失败返回null
     */
    public Long incr(String key) {
        return incrBy(key, 1);
    }

    /**
     * 自减操作
     *
     * @param key 键
     * @return 操作后的值，失败返回null
     */
    public Long decr(String key) {
        return incrBy(key, -1);
    }

    /**
     * 获取哈希表中的对象
     *
     * @param <T> 对象类型
     * @param key 键
     * @param field 字段
     * @param clazz 对象类型
     * @return 对象，字段不存在或操作异常时返回null
     */
    public <T> T hGet(String key, String field, Class<T> clazz) {
        try {
            return redisTemplate.execute((RedisCallback<T>)con -> {
                byte[] val = con.hashCommands().hGet(keyBytes(key), field.getBytes(CHARSET));
                return bytesToObj(val, clazz);
            });
        } catch (Exception e) {
            log.error("Redis获取哈希对象失败: key={}, field={}, error={}", key, field, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 设置哈希表中的对象
     *
     * @param <T> 值的类型
     * @param key 键
     * @param field 字段
     * @param value 值（可以是字符串或任意对象）
     * @return 是否成功，操作异常时返回false
     */
    public <T> Boolean hSet(String key, String field, T value) {
        try {
            byte[] valueBytes = valueBytes(value);
            if (valueBytes == null) {
                log.warn("Redis设置哈希对象失败: key={}, field={}, 对象序列化结果为null", key, field);
                return false;
            }

            return redisTemplate.execute((RedisCallback<
                Boolean>)con -> con.hashCommands().hSet(keyBytes(key), field.getBytes(CHARSET), valueBytes));
        } catch (Exception e) {
            log.error("Redis设置哈希对象失败: key={}, field={}, error={}", key, field, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 批量设置哈希表中的值
     *
     * @param <T> 值的类型
     * @param key 键
     * @param map 键值对
     * @return 是否成功，操作异常时返回false
     */
    public <T> Boolean hMSet(String key, Map<String, T> map) {
        if (map == null || map.isEmpty()) {
            return true;
        }

        try {
            return redisTemplate.execute((RedisCallback<Boolean>)con -> {
                Map<byte[], byte[]> byteMap = new java.util.HashMap<>(map.size());
                for (Map.Entry<String, T> entry : map.entrySet()) {
                    byte[] fieldBytes = entry.getKey().getBytes(CHARSET);
                    byte[] valueBytes = valueBytes(entry.getValue());
                    if (valueBytes != null) {
                        byteMap.put(fieldBytes, valueBytes);
                    }
                }
                con.hashCommands().hMSet(keyBytes(key), byteMap);
                return true;
            });
        } catch (Exception e) {
            log.error("Redis批量设置哈希值失败: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取哈希表中的所有字段
     *
     * @param key 键
     * @return 字段集合，操作异常时返回空集合
     */
    public Set<String> hKeys(String key) {
        try {
            return redisTemplate.execute((RedisCallback<Set<String>>)con -> {
                Set<byte[]> fieldBytes = con.hashCommands().hKeys(keyBytes(key));
                if (fieldBytes == null || fieldBytes.isEmpty()) {
                    return Set.of();
                }

                Set<String> fields = new java.util.HashSet<>(fieldBytes.size());
                for (byte[] bytes : fieldBytes) {
                    fields.add(new String(bytes, CHARSET));
                }
                return fields;
            });
        } catch (Exception e) {
            log.error("Redis获取哈希字段集合失败: key={}, error={}", key, e.getMessage(), e);
            return Set.of();
        }
    }

    /**
     * 删除哈希表中的字段
     *
     * @param key 键
     * @param fields 字段
     * @return 删除的字段数量，操作异常时返回0
     */
    public Long hDel(String key, String... fields) {
        if (fields == null || fields.length == 0) {
            return 0L;
        }

        try {
            return redisTemplate.execute((RedisCallback<Long>)con -> {
                byte[][] fieldBytes = new byte[fields.length][];
                for (int i = 0; i < fields.length; i++) {
                    fieldBytes[i] = fields[i].getBytes(CHARSET);
                }
                return con.hashCommands().hDel(keyBytes(key), fieldBytes);
            });
        } catch (Exception e) {
            log.error("Redis删除哈希字段失败: key={}, fields={}, error={}", key, fields, e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * 判断哈希表中是否有该字段
     *
     * @param key 键
     * @param field 字段
     * @return 是否存在，操作异常时返回false
     */
    public Boolean hHasKey(String key, String field) {
        try {
            return redisTemplate.execute(
                (RedisCallback<Boolean>)con -> con.hashCommands().hExists(keyBytes(key), field.getBytes(CHARSET)));
        } catch (Exception e) {
            log.error("Redis判断哈希字段是否存在失败: key={}, field={}, error={}", key, field, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 哈希表中字段自增
     *
     * @param key 键
     * @param field 字段
     * @param delta 增量
     * @return 增加后的值，操作异常时返回null
     */
    public Long hIncrBy(String key, String field, long delta) {
        try {
            return redisTemplate.execute(
                (RedisCallback<Long>)con -> con.hashCommands().hIncrBy(keyBytes(key), field.getBytes(CHARSET), delta));
        } catch (Exception e) {
            log.error("Redis哈希字段自增失败: key={}, field={}, delta={}, error={}", key, field, delta, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 设置缓存对象（使用高级API）
     *
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 是否成功，操作异常时返回false
     */
    public Boolean set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            return true;
        } catch (Exception e) {
            log.error("Redis设置对象失败: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 设置缓存对象（使用高级API，不过期）
     *
     * @param key 键
     * @param value 值
     * @return 是否成功，操作异常时返回false
     */
    public Boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis设置对象失败: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取缓存对象（使用高级API）
     *
     * @param <T> 返回类型
     * @param key 键
     * @return 值，键不存在或操作异常时返回null
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        try {
            return (T)redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis获取对象失败: key={}, error={}", key, e.getMessage(), e);
            return null;
        }
    }
}
