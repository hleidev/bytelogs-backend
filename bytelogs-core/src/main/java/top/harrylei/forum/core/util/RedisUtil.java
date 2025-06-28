package top.harrylei.forum.core.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 *
 * @author harry
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
    private static void validateNotNull(Object... args) {
        for (Object obj : args) {
            if (obj == null) {
                throw new IllegalArgumentException("Redis operation argument cannot be null");
            }
        }
    }

    /**
     * 将字符串转换为字节数组
     *
     * @param key 字符串
     * @return 字节数组
     */
    private byte[] keyToBytes(String key) {
        validateNotNull(key);
        return key.getBytes(CHARSET);
    }

    /**
     * 将对象转换为字节数组
     *
     * @param <T>   对象类型
     * @param value 要序列化的对象
     * @return 字节数组，序列化失败返回null
     */
    private <T> byte[] valueToBytes(T value) {
        if (value == null) {
            return null;
        }

        try {
            if (value instanceof String) {
                return ((String) value).getBytes(CHARSET);
            }
            // 使用统一的JsonUtil进行序列化
            return JsonUtil.toBytes(value);
        } catch (Exception e) {
            log.error("Failed to serialize object: type={}, error={}", value.getClass().getName(), e.getMessage(), e);
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
     * @param <T>   对象类型
     * @param bytes 字节数组
     * @param clazz 对象类型
     * @return 反序列化后的对象，反序列化失败或bytes为null时返回null
     */
    @SuppressWarnings("unchecked")
    private <T> T bytesToObject(byte[] bytes, Class<T> clazz) {
        if (bytes == null) {
            return null;
        }

        if (String.class.equals(clazz)) {
            return (T) new String(bytes, CHARSET);
        }

        try {
            // 使用统一的JsonUtil进行反序列化
            return JsonUtil.fromBytes(bytes, clazz);
        } catch (Exception e) {
            log.error("Failed to deserialize object: targetType={}, error={}", clazz.getName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 设置对象值，带过期时间
     *
     * @param <T>     对象类型
     * @param key     键
     * @param value   值
     * @param seconds 过期时间（秒）
     * @return 是否成功，操作异常时返回false
     */
    public <T> Boolean setObjectWithExpire(String key, T value, long seconds) {
        validateNotNull(key, value);
        try {
            return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                byte[] keyBytes = keyToBytes(key);
                byte[] valueBytes = valueToBytes(value);
                if (valueBytes == null) {
                    log.warn("Failed to set object, serialization returned null: key={}", key);
                    return false;
                }
                if (seconds > 0) {
                    return connection.stringCommands().set(keyBytes, valueBytes, Expiration.seconds(seconds),
                                                           RedisStringCommands.SetOption.UPSERT);
                } else {
                    return connection.stringCommands().set(keyBytes, valueBytes);
                }
            });
        } catch (Exception e) {
            log.error("Failed to set object: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 设置对象值（不过期）
     *
     * @param <T>   对象类型
     * @param key   键
     * @param value 值
     * @return 是否成功，操作异常时返回false
     */
    public <T> Boolean setObject(String key, T value) {
        return setObjectWithExpire(key, value, 0);
    }

    /**
     * 获取对象值
     *
     * @param <T>   对象类型
     * @param key   键
     * @param clazz 对象类型
     * @return 对象，键不存在或操作异常时返回null
     */
    public <T> T getObject(String key, Class<T> clazz) {
        validateNotNull(key, clazz);
        try {
            return redisTemplate.execute((RedisCallback<T>) connection -> {
                byte[] valueBytes = connection.stringCommands().get(keyToBytes(key));
                return bytesToObject(valueBytes, clazz);
            });
        } catch (Exception e) {
            log.error("Failed to get object: key={}, error={}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 删除键
     *
     * @param key 键
     * @return 是否成功，操作异常时返回false
     */
    public Boolean delete(String key) {
        validateNotNull(key);
        try {
            return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                Long result = connection.keyCommands().del(keyToBytes(key));
                return result != null && result > 0;
            });
        } catch (Exception e) {
            log.error("Failed to delete key: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 批量删除键
     *
     * @param keys 键集合
     * @return 删除的键数量，操作异常时返回0
     */
    public Long deleteKeys(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }

        try {
            return redisTemplate.execute((RedisCallback<Long>) connection -> {
                byte[][] keyByteArray = keys.stream()
                        .map(this::keyToBytes)
                        .toArray(byte[][]::new);
                return connection.keyCommands().del(keyByteArray);
            });
        } catch (Exception e) {
            log.error("Failed to delete keys in batch: keys={}, error={}", keys, e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * 批量删除键（可变参数版本）
     *
     * @param keys 键数组
     * @return 删除的键数量，操作异常时返回0
     */
    public Long deleteAll(String... keys) {
        if (keys == null || keys.length == 0) {
            return 0L;
        }
        return deleteKeys(Arrays.asList(keys));
    }

    /**
     * 判断键是否存在
     *
     * @param key 键
     * @return 是否存在，操作异常时返回false
     */
    public Boolean exists(String key) {
        validateNotNull(key);
        try {
            return redisTemplate.execute((RedisCallback<Boolean>) connection ->
                    connection.keyCommands().exists(keyToBytes(key)));
        } catch (Exception e) {
            log.error("Failed to check key existence: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 设置键的过期时间
     *
     * @param key     键
     * @param seconds 过期时间（秒）
     * @return 是否成功，操作异常时返回false
     */
    public Boolean expire(String key, long seconds) {
        validateNotNull(key);
        if (seconds <= 0) {
            throw new IllegalArgumentException("Expire seconds must be positive");
        }
        try {
            return redisTemplate.execute((RedisCallback<Boolean>) connection ->
                    connection.keyCommands().expire(keyToBytes(key), seconds));
        } catch (Exception e) {
            log.error("Failed to set expiration: key={}, seconds={}, error={}", key, seconds, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取键的剩余生存时间
     *
     * @param key 键
     * @return 剩余生存时间（秒），-1表示永不过期，-2表示键不存在或操作异常
     */
    public Long ttl(String key) {
        validateNotNull(key);
        try {
            return redisTemplate.execute((RedisCallback<Long>) connection ->
                    connection.keyCommands().ttl(keyToBytes(key)));
        } catch (Exception e) {
            log.error("Failed to get TTL: key={}, error={}", key, e.getMessage(), e);
            return -2L;
        }
    }

    /**
     * 自增/自减操作
     *
     * @param key   键
     * @param delta 增量（正值为自增，负值为自减）
     * @return 操作后的值，失败返回null
     */
    public Long incrementBy(String key, long delta) {
        validateNotNull(key);
        try {
            return redisTemplate.execute((RedisCallback<Long>) connection ->
                    connection.stringCommands().incrBy(keyToBytes(key), delta));
        } catch (Exception e) {
            log.error("Failed to increment by delta: key={}, delta={}, error={}", key, delta, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 自增操作
     *
     * @param key 键
     * @return 操作后的值，失败返回null
     */
    public Long increment(String key) {
        return incrementBy(key, 1);
    }

    /**
     * 自减操作
     *
     * @param key 键
     * @return 操作后的值，失败返回null
     */
    public Long decrement(String key) {
        return incrementBy(key, -1);
    }

    /**
     * 自减操作，指定减量
     *
     * @param key   键
     * @param delta 减量（正值）
     * @return 操作后的值，失败返回null
     */
    public Long decrementBy(String key, long delta) {
        return incrementBy(key, -Math.abs(delta));
    }

    /**
     * 获取哈希表中的对象
     *
     * @param <T>   对象类型
     * @param key   键
     * @param field 字段
     * @param clazz 对象类型
     * @return 对象，字段不存在或操作异常时返回null
     */
    public <T> T hGet(String key, String field, Class<T> clazz) {
        validateNotNull(key, field, clazz);
        try {
            return redisTemplate.execute((RedisCallback<T>) connection -> {
                byte[] valueBytes = connection.hashCommands().hGet(keyToBytes(key), field.getBytes(CHARSET));
                return bytesToObject(valueBytes, clazz);
            });
        } catch (Exception e) {
            log.error("Failed to get hash field: key={}, field={}, error={}", key, field, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 设置哈希表中的对象
     *
     * @param <T>   值的类型
     * @param key   键
     * @param field 字段
     * @param value 值（可以是字符串或任意对象）
     * @return 是否成功，操作异常时返回false
     */
    public <T> Boolean hSet(String key, String field, T value) {
        validateNotNull(key, field, value);
        try {
            byte[] valueBytes = valueToBytes(value);
            if (valueBytes == null) {
                log.warn("Failed to set hash field, serialization returned null: key={}, field={}", key, field);
                return false;
            }

            return redisTemplate.execute((RedisCallback<Boolean>) connection ->
                    connection.hashCommands().hSet(keyToBytes(key), field.getBytes(CHARSET), valueBytes));
        } catch (Exception e) {
            log.error("Failed to set hash field: key={}, field={}, error={}", key, field, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 批量设置哈希表中的值
     *
     * @param <T>           值的类型
     * @param key           键
     * @param fieldValueMap 字段值映射
     * @return 是否成功，操作异常时返回false
     */
    public <T> Boolean hSetAll(String key, Map<String, T> fieldValueMap) {
        validateNotNull(key);
        if (fieldValueMap == null || fieldValueMap.isEmpty()) {
            return true;
        }

        try {
            return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                Map<byte[], byte[]> byteMap = new HashMap<>(fieldValueMap.size());
                for (Map.Entry<String, T> entry : fieldValueMap.entrySet()) {
                    byte[] fieldBytes = entry.getKey().getBytes(CHARSET);
                    byte[] valueBytes = valueToBytes(entry.getValue());
                    if (valueBytes != null) {
                        byteMap.put(fieldBytes, valueBytes);
                    }
                }
                connection.hashCommands().hMSet(keyToBytes(key), byteMap);
                return true;
            });
        } catch (Exception e) {
            log.error("Failed to set hash fields in batch: key={}, error={}", key, e.getMessage(), e);
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
        validateNotNull(key);
        try {
            return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
                Set<byte[]> fieldBytes = connection.hashCommands().hKeys(keyToBytes(key));
                if (fieldBytes == null || fieldBytes.isEmpty()) {
                    return Set.of();
                }

                Set<String> fields = new HashSet<>(fieldBytes.size());
                for (byte[] bytes : fieldBytes) {
                    fields.add(new String(bytes, CHARSET));
                }
                return fields;
            });
        } catch (Exception e) {
            log.error("Failed to get hash keys: key={}, error={}", key, e.getMessage(), e);
            return Set.of();
        }
    }

    /**
     * 删除哈希表中的字段
     *
     * @param key    键
     * @param fields 字段
     * @return 删除的字段数量，操作异常时返回0
     */
    public Long hDelete(String key, String... fields) {
        validateNotNull(key);
        if (fields == null || fields.length == 0) {
            return 0L;
        }

        try {
            return redisTemplate.execute((RedisCallback<Long>) connection -> {
                byte[][] fieldBytes = new byte[fields.length][];
                for (int i = 0; i < fields.length; i++) {
                    fieldBytes[i] = fields[i].getBytes(CHARSET);
                }
                return connection.hashCommands().hDel(keyToBytes(key), fieldBytes);
            });
        } catch (Exception e) {
            log.error("Failed to delete hash fields: key={}, fields={}, error={}",
                      key,
                      Arrays.toString(fields),
                      e.getMessage(),
                      e);
            return 0L;
        }
    }

    /**
     * 判断哈希表中是否存在该字段
     *
     * @param key   键
     * @param field 字段
     * @return 是否存在，操作异常时返回false
     */
    public Boolean hExists(String key, String field) {
        validateNotNull(key, field);
        try {
            return redisTemplate.execute((RedisCallback<Boolean>) connection ->
                    connection.hashCommands().hExists(keyToBytes(key), field.getBytes(CHARSET)));
        } catch (Exception e) {
            log.error("Failed to check hash field existence: key={}, field={}, error={}",
                      key,
                      field,
                      e.getMessage(),
                      e);
            return false;
        }
    }

    /**
     * 哈希表中字段自增
     *
     * @param key   键
     * @param field 字段
     * @param delta 增量
     * @return 增加后的值，操作异常时返回null
     */
    public Long hIncrementBy(String key, String field, long delta) {
        validateNotNull(key, field);
        try {
            return redisTemplate.execute((RedisCallback<Long>) connection ->
                    connection.hashCommands().hIncrBy(keyToBytes(key), field.getBytes(CHARSET), delta));
        } catch (Exception e) {
            log.error("Failed to increment hash field: key={}, field={}, delta={}, error={}",
                      key, field, delta, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 哈希表中字段自增1
     *
     * @param key   键
     * @param field 字段
     * @return 增加后的值，操作异常时返回null
     */
    public Long hIncrement(String key, String field) {
        return hIncrementBy(key, field, 1);
    }

    /**
     * 哈希表中字段自减
     *
     * @param key   键
     * @param field 字段
     * @param delta 减量（正值）
     * @return 减少后的值，操作异常时返回null
     */
    public Long hDecrementBy(String key, String field, long delta) {
        return hIncrementBy(key, field, -Math.abs(delta));
    }

    /**
     * 哈希表中字段自减1
     *
     * @param key   键
     * @param field 字段
     * @return 减少后的值，操作异常时返回null
     */
    public Long hDecrement(String key, String field) {
        return hIncrementBy(key, field, -1);
    }

    /**
     * 设置缓存对象（使用高级API）
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 是否成功，操作异常时返回false
     */
    public Boolean set(String key, Object value, long timeout, TimeUnit unit) {
        validateNotNull(key, value, unit);
        if (timeout <= 0) {
            throw new IllegalArgumentException("Timeout must be positive");
        }
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            return true;
        } catch (Exception e) {
            log.error("Failed to set value with timeout: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 设置缓存对象（使用高级API，不过期）
     *
     * @param key   键
     * @param value 值
     * @return 是否成功，操作异常时返回false
     */
    public Boolean set(String key, Object value) {
        validateNotNull(key, value);
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("Failed to set value: key={}, error={}", key, e.getMessage(), e);
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
        validateNotNull(key);
        try {
            return (T) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Failed to get value: key={}, error={}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取字符串值
     *
     * @param key 键
     * @return 字符串值，键不存在或操作异常时返回null
     */
    public String getString(String key) {
        return getObject(key, String.class);
    }

    /**
     * 设置字符串值
     *
     * @param key   键
     * @param value 字符串值
     * @return 是否成功，操作异常时返回false
     */
    public Boolean setString(String key, String value) {
        return setObject(key, value);
    }

    /**
     * 设置字符串值，带过期时间
     *
     * @param key     键
     * @param value   字符串值
     * @param seconds 过期时间（秒）
     * @return 是否成功，操作异常时返回false
     */
    public Boolean setStringWithExpire(String key, String value, long seconds) {
        return setObjectWithExpire(key, value, seconds);
    }

    /**
     * 使用Duration设置过期时间
     *
     * @param key      键
     * @param value    值
     * @param duration 过期时间
     * @return 是否成功
     */
    public <T> Boolean setObjectWithExpire(String key, T value, Duration duration) {
        validateNotNull(duration);
        return setObjectWithExpire(key, value, duration.getSeconds());
    }

    /**
     * 批量获取对象
     *
     * @param keys  键集合
     * @param clazz 对象类型
     * @return 键值对映射
     */
    public <T> Map<String, T> getObjects(Collection<String> keys, Class<T> clazz) {
        if (keys == null || keys.isEmpty()) {
            return Map.of();
        }

        Map<String, T> result = new HashMap<>(keys.size());
        for (String key : keys) {
            T value = getObject(key, clazz);
            if (value != null) {
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * 批量设置对象
     *
     * @param keyValueMap 键值对映射
     * @return 成功设置的数量
     */
    public <T> Long setObjects(Map<String, T> keyValueMap) {
        if (keyValueMap == null || keyValueMap.isEmpty()) {
            return 0L;
        }

        long successCount = 0;
        for (Map.Entry<String, T> entry : keyValueMap.entrySet()) {
            if (Boolean.TRUE.equals(setObject(entry.getKey(), entry.getValue()))) {
                successCount++;
            }
        }
        return successCount;
    }

    /**
     * 检查多个键是否存在
     *
     * @param keys 键集合
     * @return 存在的键集合
     */
    public Set<String> existsKeys(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Set.of();
        }

        Set<String> existingKeys = new HashSet<>();
        for (String key : keys) {
            if (Boolean.TRUE.equals(exists(key))) {
                existingKeys.add(key);
            }
        }
        return existingKeys;
    }

    /**
     * 获取键的类型
     *
     * @param key 键
     * @return 数据类型字符串
     */
    public String type(String key) {
        validateNotNull(key);
        try {
            return redisTemplate.execute((RedisCallback<String>) connection -> {
                org.springframework.data.redis.connection.DataType dataType =
                        connection.keyCommands().type(keyToBytes(key));
                return dataType != null ? dataType.name() : "none";
            });
        } catch (Exception e) {
            log.error("Failed to get key type: key={}, error={}", key, e.getMessage(), e);
            return "unknown";
        }
    }
}
