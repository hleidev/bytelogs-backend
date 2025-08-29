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
 * 置顶状态枚举
 *
 * @author harry
 */
@Getter
@AllArgsConstructor
public enum ToppingStatusEnum {

    /**
     * 不置顶
     */
    NOT_TOPPING(0, "不置顶"),

    /**
     * 置顶
     */
    TOPPING(1, "置顶");

    @EnumValue
    private final Integer code;

    private final String label;

    private static final Map<Integer, ToppingStatusEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(ToppingStatusEnum::getCode, Function.identity()));

    @JsonValue
    public Integer getCode() {
        return code;
    }

    @JsonCreator
    public static ToppingStatusEnum fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }
}