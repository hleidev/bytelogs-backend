package top.harrylei.forum.core.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

/**
 * JSON工具类
 *
 * @author harry
 */
@Slf4j
public class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        // 配置ObjectMapper
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 支持Java 8时间类型
        MAPPER.registerModule(new JavaTimeModule());
        // 禁用将日期写为时间戳的功能
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 传统Date类型的格式
        MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

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
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("对象转JSON失败: 对象类型={}, 错误信息={}", obj.getClass().getSimpleName(), e.getMessage(), e);
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
    public static <T> T fromJson(String jsonStr, Class<T> clazz) {
        if (StringUtils.isBlank(jsonStr) || clazz == null) {
            return null;
        }

        try {
            return MAPPER.readValue(jsonStr, clazz);
        } catch (Exception e) {
            log.error("JSON转对象失败: JSON={}, 目标类型={}, 错误信息={}",
                      jsonStr, clazz.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将对象转换为字节数组
     *
     * @param obj 要转换的对象
     * @return 字节数组，失败返回null
     */
    public static byte[] toBytes(Object obj) {
        String jsonStr = toJson(obj);
        if (jsonStr == null) {
            return null;
        }
        return jsonStr.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 从字节数组转换为指定类型对象
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
            String jsonStr = new String(bytes, StandardCharsets.UTF_8);
            return fromJson(jsonStr, clazz);
        } catch (Exception e) {
            log.error("字节数组转对象失败: 目标类型={}, 错误信息={}", clazz.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将JSON字符串解析为JsonNode对象
     *
     * @param jsonStr JSON字符串
     * @return JsonNode对象，失败返回null
     */
    public static JsonNode parseToNode(String jsonStr) {
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }

        try {
            return MAPPER.readTree(jsonStr);
        } catch (Exception e) {
            log.error("JSON解析为JsonNode失败: JSON={}, 错误信息={}", jsonStr, e.getMessage(), e);
            return null;
        }
    }
}