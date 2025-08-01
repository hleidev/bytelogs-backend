package top.harrylei.forum.core.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;
import top.harrylei.forum.core.common.constans.RedisKeyConstants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

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
    private static final String UNLOCK_LUA_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

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
                throw new IllegalArgumentException("Redis操作参数不能为空");
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
            log.error("对象序列化失败: type={}, error={}", value.getClass().getName(), e.getMessage(), e);
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
            log.error("对象反序列化失败: targetType={}, error={}", clazz.getName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 设置值（不过期）
     *
     * @param <T>   值类型
     * @param key   键
     * @param value 值
     * @return 是否成功，操作异常时返回false
     */
    public <T> Boolean set(String key, T value) {
        return set(key, value, 0);
    }

    /**
     * 设置值，带过期时间
     *
     * @param <T>     值类型
     * @param key     键
     * @param value   值
     * @param seconds 过期时间（秒）
     * @return 是否成功，操作异常时返回false
     */
    public <T> Boolean set(String key, T value, long seconds) {
        validateNotNull(key, value);
        try {
            return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                byte[] keyBytes = keyToBytes(key);
                byte[] valueBytes = valueToBytes(value);
                if (valueBytes == null) {
                    log.warn("设置值失败，序列化返回null: key={}", key);
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
            log.error("设置值失败: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 设置值，带过期时间（Duration版本）
     *
     * @param <T>      值类型
     * @param key      键
     * @param value    值
     * @param duration 过期时间
     * @return 是否成功，操作异常时返回false
     */
    public <T> Boolean set(String key, T value, Duration duration) {
        validateNotNull(key, value);
        long seconds = duration.getSeconds();
        if (seconds < 0) {
            throw new IllegalArgumentException("Duration 不得为负数");
        }
        return set(key, value, seconds);
    }

    /**
     * 仅当键不存在时设置值（不过期）
     *
     * @param <T>   值类型
     * @param key   键
     * @param value 值
     * @return 是否成功设置，键已存在或操作异常时返回false
     */
    public <T> Boolean setIfAbsent(String key, T value) {
        validateNotNull(key, value);
        try {
            return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                byte[] keyBytes = keyToBytes(key);
                byte[] valueBytes = valueToBytes(value);
                if (valueBytes == null) {
                    log.warn("如果不存在，则设置值失败，序列化返回 null : key={}", key);
                    return false;
                }
                return connection.stringCommands().set(keyBytes, valueBytes,
                                                       Expiration.persistent(),
                                                       RedisStringCommands.SetOption.SET_IF_ABSENT);
            });
        } catch (Exception e) {
            log.error("setIfAbsent设置值失败: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 仅当键不存在时设置值，带过期时间
     *
     * @param <T>     值类型
     * @param key     键
     * @param value   值
     * @param seconds 过期时间（秒）
     * @return 是否成功设置，键已存在或操作异常时返回false
     */
    public <T> Boolean setIfAbsent(String key, T value, long seconds) {
        validateNotNull(key, value);
        if (seconds <= 0) {
            throw new IllegalArgumentException("过期时间必须为正数");
        }
        try {
            return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                byte[] keyBytes = keyToBytes(key);
                byte[] valueBytes = valueToBytes(value);
                if (valueBytes == null) {
                    log.warn("setIfAbsent带过期时间设置值失败，序列化返回null: key={}", key);
                    return false;
                }
                return connection.stringCommands().set(keyBytes, valueBytes,
                                                       Expiration.seconds(seconds),
                                                       RedisStringCommands.SetOption.SET_IF_ABSENT);
            });
        } catch (Exception e) {
            log.error("setIfAbsent带过期时间设置值失败: key={}, seconds={}, error={}",
                      key, seconds, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 仅当键不存在时设置值，带过期时间（Duration版本）
     *
     * @param <T>      值类型
     * @param key      键
     * @param value    值
     * @param duration 过期时间
     * @return 是否成功设置，键已存在或操作异常时返回false
     */
    public <T> Boolean setIfAbsent(String key, T value, Duration duration) {
        validateNotNull(key, value);
        long seconds = duration.getSeconds();
        if (seconds <= 0) {
            throw new IllegalArgumentException("setIfAbsent带过期时间的Duration必须为正数");
        }
        return setIfAbsent(key, value, seconds);
    }

    /**
     * 获取值
     *
     * @param <T>   值类型
     * @param key   键
     * @param clazz 值类型
     * @return 值，键不存在或操作异常时返回null
     */
    public <T> T get(String key, Class<T> clazz) {
        validateNotNull(key, clazz);
        try {
            return redisTemplate.execute((RedisCallback<T>) connection -> {
                byte[] valueBytes = connection.stringCommands().get(keyToBytes(key));
                return bytesToObject(valueBytes, clazz);
            });
        } catch (Exception e) {
            log.error("获取值失败: key={}, error={}", key, e.getMessage(), e);
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
        validateNotNull(key);
        try {
            return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                Long result = connection.keyCommands().del(keyToBytes(key));
                return result != null && result > 0;
            });
        } catch (Exception e) {
            log.error("删除键失败: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 批量删除键
     *
     * @param keys 键集合
     * @return 删除的键数量，操作异常时返回0
     */
    public Long delAll(Collection<String> keys) {
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
            log.error("批量删除键失败: keys={}, error={}", keys, e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * 批量删除键（可变参数版本）
     *
     * @param keys 键数组
     * @return 删除的键数量，操作异常时返回0
     */
    public Long delAll(String... keys) {
        if (keys == null || keys.length == 0) {
            return 0L;
        }
        return delAll(Arrays.asList(keys));
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
            log.error("检查键是否存在失败: key={}, error={}", key, e.getMessage(), e);
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
            throw new IllegalArgumentException("过期时间必须为正数");
        }
        try {
            return redisTemplate.execute((RedisCallback<Boolean>) connection ->
                    connection.keyCommands().expire(keyToBytes(key), seconds));
        } catch (Exception e) {
            log.error("设置过期时间失败: key={}, seconds={}, error={}", key, seconds, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 设置键的过期时间（Duration版本）
     *
     * @param key      键
     * @param duration 过期时间
     * @return 是否成功，操作异常时返回false
     */
    public Boolean expire(String key, Duration duration) {
        validateNotNull(key);
        long seconds = duration.getSeconds();
        if (seconds <= 0) {
            throw new IllegalArgumentException("过期时间Duration必须为正数");
        }
        return expire(key, seconds);
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
            log.error("获取TTL失败: key={}, error={}", key, e.getMessage(), e);
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
    public Long incrBy(String key, long delta) {
        validateNotNull(key);
        try {
            return redisTemplate.execute((RedisCallback<Long>) connection ->
                    connection.stringCommands().incrBy(keyToBytes(key), delta));
        } catch (Exception e) {
            log.error("增量操作失败: key={}, delta={}, error={}", key, delta, e.getMessage(), e);
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
     * 自减操作，指定减量
     *
     * @param key   键
     * @param delta 减量（正值）
     * @return 操作后的值，失败返回null
     */
    public Long decrBy(String key, long delta) {
        return incrBy(key, -Math.abs(delta));
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
            log.error("获取哈希字段失败: key={}, field={}, error={}", key, field, e.getMessage(), e);
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
                log.warn("设置哈希字段失败，序列化返回null: key={}, field={}", key, field);
                return false;
            }

            return redisTemplate.execute((RedisCallback<Boolean>) connection ->
                    connection.hashCommands().hSet(keyToBytes(key), field.getBytes(CHARSET), valueBytes));
        } catch (Exception e) {
            log.error("设置哈希字段失败: key={}, field={}, error={}", key, field, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 批量设置哈希表中的值
     *
     * @param <T> 值的类型
     * @param key 键
     * @param map 字段值映射
     * @return 是否成功，操作异常时返回false
     */
    public <T> Boolean hSetAll(String key, Map<String, T> map) {
        validateNotNull(key);
        if (map == null || map.isEmpty()) {
            return true;
        }

        try {
            return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                Map<byte[], byte[]> byteMap = new HashMap<>(map.size());
                for (Map.Entry<String, T> entry : map.entrySet()) {
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
            log.error("批量设置哈希字段失败: key={}, error={}", key, e.getMessage(), e);
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
            log.error("获取哈希键失败: key={}, error={}", key, e.getMessage(), e);
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
    public Long hDel(String key, String... fields) {
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
            log.error("删除哈希字段失败: key={}, fields={}, error={}",
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
            log.error("检查哈希字段是否存在失败: key={}, field={}, error={}",
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
    public Long hIncrBy(String key, String field, long delta) {
        validateNotNull(key, field);
        try {
            return redisTemplate.execute((RedisCallback<Long>) connection ->
                    connection.hashCommands().hIncrBy(keyToBytes(key), field.getBytes(CHARSET), delta));
        } catch (Exception e) {
            log.error("哈希字段自增失败: key={}, field={}, delta={}, error={}",
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
    public Long hIncr(String key, String field) {
        return hIncrBy(key, field, 1);
    }

    /**
     * 哈希表中字段自减
     *
     * @param key   键
     * @param field 字段
     * @param delta 减量（正值）
     * @return 减少后的值，操作异常时返回null
     */
    public Long hDecrBy(String key, String field, long delta) {
        return hIncrBy(key, field, -Math.abs(delta));
    }

    /**
     * 哈希表中字段自减1
     *
     * @param key   键
     * @param field 字段
     * @return 减少后的值，操作异常时返回null
     */
    public Long hDecr(String key, String field) {
        return hIncrBy(key, field, -1);
    }

    /**
     * 批量获取值
     *
     * @param keys  键集合
     * @param clazz 值类型
     * @return 键值对映射
     */
    public <T> Map<String, T> getAll(Collection<String> keys, Class<T> clazz) {
        if (keys == null || keys.isEmpty()) {
            return Map.of();
        }

        Map<String, T> result = new HashMap<>(keys.size());
        for (String key : keys) {
            T value = get(key, clazz);
            if (value != null) {
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * 批量设置值
     *
     * @param map 键值对映射
     * @return 成功设置的数量
     */
    public <T> Long setAll(Map<String, T> map) {
        if (map == null || map.isEmpty()) {
            return 0L;
        }

        long successCount = 0;
        for (Map.Entry<String, T> entry : map.entrySet()) {
            if (Boolean.TRUE.equals(set(entry.getKey(), entry.getValue()))) {
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
    public Set<String> existsAll(Collection<String> keys) {
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
     * 释放分布式锁
     *
     * @param lockKey   锁的key
     * @param lockValue 锁的值
     * @return 是否释放成功
     */
    public boolean releaseLock(String lockKey, String lockValue) {
        validateNotNull(lockKey, lockValue);
        String key = RedisKeyConstants.getDistributedLockKey(lockKey);

        try {
            // 执行Lua脚本
            return Boolean.TRUE.equals(redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                // 使用与设置锁相同的字节数组方式
                byte[] keyBytes = keyToBytes(key);
                byte[] valueBytes = valueToBytes(lockValue);

                // 执行Lua脚本
                Long result = connection.scriptingCommands().eval(
                        UNLOCK_LUA_SCRIPT.getBytes(CHARSET),
                        org.springframework.data.redis.connection.ReturnType.INTEGER,
                        1,
                        keyBytes,
                        valueBytes
                );

                boolean success = Long.valueOf(1).equals(result);
                if (success) {
                    log.debug("释放分布式锁成功: key={}, value={}", key, lockValue);
                } else {
                    log.debug("释放分布式锁失败（锁可能已过期或值不匹配）: key={}, value={}", key, lockValue);
                }

                return success;
            }));
        } catch (Exception e) {
            log.error("释放分布式锁异常: key={}, value={}, error={}", key, lockValue, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 防重复提交锁（自动过期，无需手动释放）
     *
     * @param lockKey 锁的key
     * @param seconds 锁过期时间（秒）
     * @return 是否获取锁成功（false表示重复提交）
     */
    public boolean tryPreventDuplicate(String lockKey, long seconds) {
        validateNotNull(lockKey);
        if (seconds <= 0) {
            throw new IllegalArgumentException("防重复提交的过期时间必须为正数");
        }

        String lockValue = UUID.randomUUID().toString();
        String key = RedisKeyConstants.getDuplicateLockKey(lockKey);

        Boolean success = setIfAbsent(key, lockValue, seconds);
        if (Boolean.TRUE.equals(success)) {
            log.debug("获取防重复提交锁成功: key={}, expireSeconds={}", key, seconds);
            return true;
        }

        log.debug("获取防重复提交锁失败，检测到重复提交: key={}", key);
        return false;
    }

    /**
     * 防重复提交锁（Duration版本）
     *
     * @param lockKey  锁的key
     * @param duration 锁过期时间
     * @return 是否获取锁成功（false表示重复提交）
     */
    public boolean tryPreventDuplicate(String lockKey, Duration duration) {
        validateNotNull(duration);
        long seconds = duration.getSeconds();
        if (seconds <= 0) {
            throw new IllegalArgumentException("防重复提交的Duration必须为正数");
        }
        return tryPreventDuplicate(lockKey, seconds);
    }

    /**
     * 向有序集合添加成员
     *
     * @param key    键
     * @param member 成员
     * @param score  分数
     * @return 是否成功，操作异常时返回false
     */
    public Boolean zAdd(String key, String member, double score) {
        validateNotNull(key, member);
        try {
            return redisTemplate.execute((RedisCallback<Boolean>) connection ->
                    connection.zSetCommands().zAdd(keyToBytes(key), score, member.getBytes(CHARSET)));
        } catch (Exception e) {
            log.error("添加有序集合成员失败: key={}, member={}, score={}, error={}",
                      key, member, score, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 增加有序集合成员的分数
     *
     * @param key    键
     * @param member 成员
     * @param delta  分数增量
     * @return 增加后的分数，操作异常时返回null
     */
    public Double zIncrBy(String key, String member, double delta) {
        validateNotNull(key, member);
        try {
            return redisTemplate.execute((RedisCallback<Double>) connection ->
                    connection.zSetCommands().zIncrBy(keyToBytes(key), delta, member.getBytes(CHARSET)));
        } catch (Exception e) {
            log.error("增加有序集合成员分数失败: key={}, member={}, delta={}, error={}",
                      key, member, delta, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取有序集合成员的分数
     *
     * @param key    键
     * @param member 成员
     * @return 分数，成员不存在或操作异常时返回null
     */
    public Double zScore(String key, String member) {
        validateNotNull(key, member);
        try {
            return redisTemplate.execute((RedisCallback<Double>) connection ->
                    connection.zSetCommands().zScore(keyToBytes(key), member.getBytes(CHARSET)));
        } catch (Exception e) {
            log.error("获取有序集合成员分数失败: key={}, member={}, error={}",
                      key, member, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取有序集合成员的排名（从小到大）
     *
     * @param key    键
     * @param member 成员
     * @return 排名，成员不存在或操作异常时返回null
     */
    public Long zRank(String key, String member) {
        validateNotNull(key, member);
        try {
            return redisTemplate.execute((RedisCallback<Long>) connection ->
                    connection.zSetCommands().zRank(keyToBytes(key), member.getBytes(CHARSET)));
        } catch (Exception e) {
            log.error("获取有序集合成员排名失败: key={}, member={}, error={}",
                      key, member, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取有序集合成员的逆序排名（从大到小）
     *
     * @param key    键
     * @param member 成员
     * @return 排名，成员不存在或操作异常时返回null
     */
    public Long zRevRank(String key, String member) {
        validateNotNull(key, member);
        try {
            return redisTemplate.execute((RedisCallback<Long>) connection ->
                    connection.zSetCommands().zRevRank(keyToBytes(key), member.getBytes(CHARSET)));
        } catch (Exception e) {
            log.error("获取有序集合成员逆序排名失败: key={}, member={}, error={}",
                      key, member, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 删除有序集合成员
     *
     * @param key     键
     * @param members 成员数组
     * @return 删除的成员数量，操作异常时返回0
     */
    public Long zRem(String key, String... members) {
        validateNotNull(key);
        if (members == null || members.length == 0) {
            return 0L;
        }

        try {
            return redisTemplate.execute((RedisCallback<Long>) connection -> {
                byte[][] memberBytes = new byte[members.length][];
                for (int i = 0; i < members.length; i++) {
                    memberBytes[i] = members[i].getBytes(CHARSET);
                }
                return connection.zSetCommands().zRem(keyToBytes(key), memberBytes);
            });
        } catch (Exception e) {
            log.error("删除有序集合成员失败: key={}, members={}, error={}",
                      key, Arrays.toString(members), e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * 获取有序集合成员数量
     *
     * @param key 键
     * @return 成员数量，操作异常时返回0
     */
    public Long zCard(String key) {
        validateNotNull(key);
        try {
            return redisTemplate.execute((RedisCallback<Long>) connection ->
                    connection.zSetCommands().zCard(keyToBytes(key)));
        } catch (Exception e) {
            log.error("获取有序集合成员数量失败: key={}, error={}", key, e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * 获取有序集合指定范围的成员和分数
     *
     * @param key   键
     * @param start 起始位置
     * @param end   结束位置
     * @return 不可变的成员和分数列表，操作异常时返回空列表
     */
    public List<Map.Entry<String, Double>> zRevRangeWithScores(String key, long start, long end) {
        validateNotNull(key, start, end);
        try {
            return redisTemplate.execute((RedisCallback<List<Map.Entry<String, Double>>>) connection -> {
                Set<Tuple> tuples = connection.zSetCommands().zRevRangeWithScores(keyToBytes(key), start, end);

                if (tuples == null || tuples.isEmpty()) {
                    return List.of();
                }

                ImmutableList.Builder<Map.Entry<String, Double>> builder = ImmutableList.builder();

                for (Tuple tuple : tuples) {
                    String member = new String(tuple.getValue(), CHARSET);
                    Double score = tuple.getScore();
                    builder.add(Maps.immutableEntry(member, score));
                }
                return builder.build();
            });
        } catch (Exception e) {
            log.error("获取有序集合逆序范围成员和分数失败: key={}, start={}, end={}, error={}",
                      key, start, end, e.getMessage(), e);
            return List.of();
        }
    }
}
