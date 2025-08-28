package top.harrylei.community.api.enums.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户状态枚举
 *
 * @author harry
 */
@Getter
@AllArgsConstructor
public enum UserStatusEnum {

    /**
     * 禁用状态
     */
    DISABLED(0, "禁用"),
    /**
     * 启用状态
     */
    ENABLED(1, "启动");

    private final int code;
    private final String label;

    // 根据角色编码快速定位枚举实例
    private static final Map<Integer, UserStatusEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(UserStatusEnum::getCode, Function.identity()));

    /**
     * 获取状态码
     *
     * @return 状态码
     */
    @JsonValue
    public Integer getCode() {
        return code;
    }

    /**
     * 根据状态编码获取枚举对象
     *
     * @param code 状态编码
     * @return 对应的状态枚举，若无匹配则返回 null
     */
    @JsonCreator
    public static UserStatusEnum fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }
}

