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
 * 官方状态枚举
 *
 * @author harry
 */
@Getter
@AllArgsConstructor
public enum OfficialStatusEnum {

    /**
     * 非官方
     */
    NOT_OFFICIAL(0, "非官方"),

    /**
     * 官方
     */
    OFFICIAL(1, "官方");

    @EnumValue
    private final Integer code;

    private final String label;

    private static final Map<Integer, OfficialStatusEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(OfficialStatusEnum::getCode, Function.identity()));

    @JsonValue
    public Integer getCode() {
        return code;
    }

    @JsonCreator
    public static OfficialStatusEnum fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }
}