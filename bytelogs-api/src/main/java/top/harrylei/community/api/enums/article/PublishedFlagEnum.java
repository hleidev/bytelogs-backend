package top.harrylei.community.api.enums.article;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 发布版本标记枚举
 *
 * @author harry
 */
@Getter
@AllArgsConstructor
public enum PublishedFlagEnum {

    /**
     * 否
     */
    NO(0, "否"),

    /**
     * 是
     */
    YES(1, "是");

    @EnumValue
    private final Integer code;

    private final String label;

    // 根据编码快速定位枚举实例
    private static final Map<Integer, PublishedFlagEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(PublishedFlagEnum::getCode, Function.identity()));

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
    public static PublishedFlagEnum fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }
}