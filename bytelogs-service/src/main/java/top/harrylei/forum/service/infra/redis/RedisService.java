package top.harrylei.forum.service.infra.redis;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis服务接口 定义Redis操作的标准方法集
 */
public interface RedisService {

    /**
     * 设置对象值
     *
     * @param <T> 对象类型
     * @param key 键
     * @param value 值
     * @param seconds 过期时间（秒）
     * @return 是否成功
     */
    <T> Boolean setObj(String key, T value, long seconds);

    /**
     * 设置对象值（不过期）
     *
     * @param <T> 对象类型
     * @param key 键
     * @param value 值
     * @return 是否成功
     */
    <T> Boolean setObj(String key, T value);

    /**
     * 获取对象值
     *
     * @param <T> 对象类型
     * @param key 键
     * @param clazz 对象类型
     * @return 对象
     */
    <T> T getObj(String key, Class<T> clazz);

    /**
     * 删除键
     *
     * @param key 键
     * @return 是否成功
     */
    Boolean del(String key);

    /**
     * 批量删除键
     *
     * @param keys 键集合
     * @return 删除的键数量
     */
    Long del(Collection<String> keys);

    /**
     * 判断键是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    Boolean hasKey(String key);

    /**
     * 设置过期时间
     *
     * @param key 键
     * @param seconds 过期时间（秒）
     * @return 是否成功
     */
    Boolean expire(String key, long seconds);

    /**
     * 获取过期时间
     *
     * @param key 键
     * @return 过期时间（秒）
     */
    Long getExpire(String key);

    /**
     * 自增/自减操作
     *
     * @param key 键
     * @param delta 增量
     * @return 操作后的值
     */
    Long incrBy(String key, long delta);

    /**
     * 自增操作
     *
     * @param key 键
     * @return 操作后的值
     */
    Long incr(String key);

    /**
     * 自减操作
     *
     * @param key 键
     * @return 操作后的值
     */
    Long decr(String key);

    /**
     * 获取哈希表中的对象
     *
     * @param <T> 对象类型
     * @param key 键
     * @param field 字段
     * @param clazz 对象类型
     * @return 对象
     */
    <T> T hGet(String key, String field, Class<T> clazz);

    /**
     * 设置哈希表中的对象
     *
     * @param <T> 值的类型
     * @param key 键
     * @param field 字段
     * @param value 值
     * @return 是否成功
     */
    <T> Boolean hSet(String key, String field, T value);

    /**
     * 批量设置哈希表中的值
     *
     * @param <T> 值的类型
     * @param key 键
     * @param map 键值对
     * @return 是否成功
     */
    <T> Boolean hMSet(String key, Map<String, T> map);

    /**
     * 获取哈希表中的所有字段
     *
     * @param key 键
     * @return 字段集合
     */
    Set<String> hKeys(String key);

    /**
     * 删除哈希表中的字段
     *
     * @param key 键
     * @param fields 字段
     * @return 删除的字段数量
     */
    Long hDel(String key, String... fields);

    /**
     * 判断哈希表中是否有该字段
     *
     * @param key 键
     * @param field 字段
     * @return 是否存在
     */
    Boolean hHasKey(String key, String field);

    /**
     * 哈希表中字段自增
     *
     * @param key 键
     * @param field 字段
     * @param delta 增量
     * @return 增加后的值
     */
    Long hIncrBy(String key, String field, long delta);

    /**
     * 设置缓存对象（使用高级API）
     *
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 是否成功
     */
    Boolean set(String key, Object value, long timeout, TimeUnit unit);

    /**
     * 设置缓存对象（使用高级API，不过期）
     *
     * @param key 键
     * @param value 值
     * @return 是否成功
     */
    Boolean set(String key, Object value);

    /**
     * 获取缓存对象（使用高级API）
     *
     * @param <T> 返回类型
     * @param key 键
     * @return 值
     */
    <T> T get(String key);
}