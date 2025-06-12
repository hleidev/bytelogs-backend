package top.harrylei.forum.api.model.enums.article;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 标签类型枚举
 */
@Getter
@AllArgsConstructor
public enum TagTypeEnum {

    /**
     * 系统标签
     */
    SYSTEM(1, "系统标签"),
    /**
     * 自定义标签
     */
    CUSTOM(2, "自定义标签");

    // 编码（唯一标识）
    private final Integer code;

    // 描述（用于展示）
    private final String label;

    // 根据编码快速定位枚举实例
    private static final Map<Integer, TagTypeEnum> CODE_MAP =
        Arrays.stream(values()).collect(Collectors.toMap(TagTypeEnum::getCode, Function.identity()));
    // 根据枚举名称（不区分大小写）快速定位枚举实例
    private static final Map<String, TagTypeEnum> NAME_MAP =
        Arrays.stream(values()).collect(Collectors.toMap(e -> e.name().toUpperCase(), Function.identity()));

    /**
     * 获取码
     *
     * @return 编码
     */
    @JsonValue
    public Integer getCode() {
        return code;
    }

    /**
     * 根据编码获取枚举对象
     *
     * @param code 编码
     * @return 对应的枚举，若无匹配则返回 null
     */
    @JsonCreator
    public static TagTypeEnum fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }

    /**
     * 根据名称获取枚举对象（忽略大小写）
     *
     * @param name 枚举名称
     * @return 对应的枚举，若无匹配或为空则返回 null
     */
    public static TagTypeEnum fromName(String name) {
        if (StringUtils.isBlank(name))
            return null;
        return NAME_MAP.get(name.toUpperCase());
    }

    /**
     * 根据编码获取描述
     *
     * @param code 编码
     * @return 标签描述，若无匹配则返回 null
     */
    public static String getLabelByCode(Integer code) {
        TagTypeEnum status = fromCode(code);
        return status == null ? null : status.getLabel();
    }
}
