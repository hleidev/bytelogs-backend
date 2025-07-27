package top.harrylei.forum.api.enums.base;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 通用枚举序列化器
 *
 * @param <T> 需要序列化的枚举类型
 * @author harry
 */
public class EnumCodeLabelJsonSerializer<T extends Enum<T> & CodeLabelEnum> extends JsonSerializer<T> {

    /**
     * 自定义序列化逻辑，将枚举输出为 {code, label} 格式。
     */
    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            // 序列化为 null
            gen.writeNull();
            return;
        }
        gen.writeStartObject();
        // 输出 code 字段
        gen.writeNumberField("code", value.getCode());
        // 输出 label 字段
        gen.writeStringField("label", value.getLabel());
        gen.writeEndObject();
    }
}