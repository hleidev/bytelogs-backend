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
 * 加精状态枚举
 *
 * @author harry
 */
@Getter
@AllArgsConstructor
public enum CreamStatusEnum {

    /**
     * 不加精
     */
    NOT_CREAM(0, "不加精"),

    /**
     * 加精
     */
    CREAM(1, "加精");

    @EnumValue
    private final Integer code;

    private final String label;

    private static final Map<Integer, CreamStatusEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(CreamStatusEnum::getCode, Function.identity()));

    @JsonValue
    public Integer getCode() {
        return code;
    }

    @JsonCreator
    public static CreamStatusEnum fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }
}