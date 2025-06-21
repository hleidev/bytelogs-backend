package top.harrylei.forum.core.common.converter;

import org.springframework.core.convert.converter.Converter;
import top.harrylei.forum.core.util.DateUtil;

import java.time.LocalDateTime;

/**
 * 全局参数转换器： 将前端传入的字符串转换为 LocalDateTime 类型，支持格式 "yyyy-MM-dd HH:mm:ss"。
 *
 * @author Harry
 */
public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {

    @Override
    public LocalDateTime convert(String source) {
        if (source.trim().isEmpty()) {
            return null;
        }

        try {
            return DateUtil.parseDateTime(source);
        } catch (Exception e) {
            throw new IllegalArgumentException("日期格式不正确，应为 yyyy-MM-dd HH:mm:ss", e);
        }
    }
}