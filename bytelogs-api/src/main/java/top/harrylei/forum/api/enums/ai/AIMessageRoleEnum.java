package top.harrylei.forum.api.enums.ai;

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
 * AI消息角色枚举
 *
 * @author harry
 */
@Getter
@AllArgsConstructor
public enum AIMessageRoleEnum {

    USER(1, "user", "用户"),
    ASSISTANT(2, "assistant", "AI助手"),
    SYSTEM(3, "system", "系统");

    @EnumValue
    private final int code;
    private final String role;
    private final String label;

    // 根据编码快速定位枚举实例
    private static final Map<Integer, AIMessageRoleEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(AIMessageRoleEnum::getCode, Function.identity()));

    /**
     * 获取编码
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
    public static AIMessageRoleEnum fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }
}