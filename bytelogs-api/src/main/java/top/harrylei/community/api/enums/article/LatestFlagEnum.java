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
 * 最新版本标记枚举
 *
 * @author harry
 */
@Getter
@AllArgsConstructor
public enum LatestFlagEnum {

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

    private static final Map<Integer, LatestFlagEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(LatestFlagEnum::getCode, Function.identity()));

    @JsonValue
    public Integer getCode() {
        return code;
    }

    @JsonCreator
    public static LatestFlagEnum fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }
}