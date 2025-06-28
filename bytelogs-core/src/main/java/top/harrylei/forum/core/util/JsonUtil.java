package top.harrylei.forum.core.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * JSON工具类
 *
 * <p>设计原则：</p>
 * <ul>
 *   <li>统一异常处理，避免业务代码处理JSON异常</li>
 *   <li>封装第三方依赖，便于后续库的升级和替换</li>
 *   <li>提供常用的JSON操作方法，提高开发效率</li>
 *   <li>支持链式调用和函数式编程</li>
 * </ul>
 *
 * @author harry
 */
@Slf4j
public class JsonUtil {

    /**
     * 私有构造函数，防止实例化
     */
    private JsonUtil() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 将对象转换为JSON字符串
     *
     * @param obj 要转换的对象
     * @return JSON字符串，失败返回null
     */
    public static String toJsonStr(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            return JSONUtil.toJsonStr(obj);
        } catch (Exception e) {
            log.error("对象转JSON失败: 对象类型={}, 错误信息={}", obj.getClass().getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将对象转换为格式化的JSON字符串（带缩进）
     *
     * @param obj 要转换的对象
     * @return 格式化的JSON字符串，失败返回null
     */
    public static String toJsonPrettyStr(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            return JSONUtil.toJsonPrettyStr(obj);
        } catch (Exception e) {
            log.error("对象转格式化JSON失败: 对象类型={}, 错误信息={}",
                      obj.getClass().getSimpleName(),
                      e.getMessage(),
                      e);
            return null;
        }
    }

    /**
     * 将JSON字符串转换为指定类型的对象
     *
     * @param jsonStr JSON字符串
     * @param clazz   目标类型
     * @param <T>     泛型类型
     * @return 转换后的对象，失败返回null
     */
    public static <T> T parseObject(String jsonStr, Class<T> clazz) {
        if (StrUtil.isBlank(jsonStr) || clazz == null) {
            return null;
        }

        try {
            return JSONUtil.toBean(jsonStr, clazz);
        } catch (Exception e) {
            log.error("JSON转对象失败: JSON={}, 目标类型={}, 错误信息={}",
                      jsonStr,
                      clazz.getSimpleName(),
                      e.getMessage(),
                      e);
            return null;
        }
    }

    /**
     * 将JSON字符串转换为List集合
     *
     * @param jsonStr JSON字符串
     * @param clazz   List元素类型
     * @param <T>     泛型类型
     * @return List对象，失败返回null
     */
    public static <T> List<T> parseList(String jsonStr, Class<T> clazz) {
        if (StrUtil.isBlank(jsonStr) || clazz == null) {
            return null;
        }

        try {
            return JSONUtil.toList(jsonStr, clazz);
        } catch (Exception e) {
            log.error("JSON转List失败: JSON={}, 元素类型={}, 错误信息={}",
                      jsonStr,
                      clazz.getSimpleName(),
                      e.getMessage(),
                      e);
            return null;
        }
    }

    /**
     * 将JSON字符串转换为Map
     *
     * @param jsonStr JSON字符串
     * @return Map对象，失败返回null
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseMap(String jsonStr) {
        if (StrUtil.isBlank(jsonStr)) {
            return null;
        }

        try {
            return JSONUtil.toBean(jsonStr, Map.class);
        } catch (Exception e) {
            log.error("JSON转Map失败: JSON={}, 错误信息={}", jsonStr, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 判断字符串是否为有效的JSON格式
     *
     * @param jsonStr 待验证的字符串
     * @return true-有效的JSON，false-无效的JSON
     */
    public static boolean isValidJson(String jsonStr) {
        if (StrUtil.isBlank(jsonStr)) {
            return false;
        }

        try {
            JSONUtil.parse(jsonStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 对象深拷贝（通过JSON序列化/反序列化实现）
     *
     * @param source      源对象
     * @param targetClass 目标类型
     * @param <T>         泛型类型
     * @return 深拷贝后的对象，失败返回null
     */
    public static <T> T deepCopy(Object source, Class<T> targetClass) {
        if (source == null || targetClass == null) {
            return null;
        }

        try {
            String jsonStr = toJsonStr(source);
            return parseObject(jsonStr, targetClass);
        } catch (Exception e) {
            log.error("对象深拷贝失败: 源类型={}, 目标类型={}, 错误信息={}",
                      source.getClass().getSimpleName(), targetClass.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将对象转换为字节数组（用于Redis等存储）
     *
     * @param obj 要转换的对象
     * @return 字节数组，失败返回null
     */
    public static byte[] toBytes(Object obj) {
        String jsonStr = toJsonStr(obj);
        if (jsonStr == null) {
            return null;
        }
        return jsonStr.getBytes();
    }

    /**
     * 从字节数组转换为指定类型对象（用于Redis等存储）
     *
     * @param bytes 字节数组
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 转换后的对象，失败返回null
     */
    public static <T> T fromBytes(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0 || clazz == null) {
            return null;
        }

        try {
            String jsonStr = new String(bytes);
            return parseObject(jsonStr, clazz);
        } catch (Exception e) {
            log.error("字节数组转对象失败: 目标类型={}, 错误信息={}", clazz.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将Map转换为指定类型的对象
     *
     * @param map   源Map
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 转换后的对象，失败返回null
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        if (map == null || map.isEmpty() || clazz == null) {
            return null;
        }

        try {
            String jsonStr = toJsonStr(map);
            return parseObject(jsonStr, clazz);
        } catch (Exception e) {
            log.error("Map转对象失败: 目标类型={}, 错误信息={}", clazz.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将对象转换为Map
     *
     * @param obj 源对象
     * @return Map对象，失败返回null
     */
    public static Map<String, Object> objectToMap(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            String jsonStr = toJsonStr(obj);
            return parseMap(jsonStr);
        } catch (Exception e) {
            log.error("对象转Map失败: 对象类型={}, 错误信息={}", obj.getClass().getSimpleName(), e.getMessage(), e);
            return null;
        }
    }
}