package top.harrylei.community.api.enums.user;

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
 * 用户角色枚举
 *
 * @author harry
 */
@Getter
@AllArgsConstructor
public enum UserRoleEnum {

    /**
     * 普通用户角色
     */
    NORMAL(0, "普通用户"),

    /**
     * 管理员角色
     */
    ADMIN(1, "管理员");

    // 角色编码（唯一标识）
    @EnumValue
    private final Integer code;

    // 角色描述（用于展示）
    private final String label;

    // 根据角色编码快速定位枚举实例
    private static final Map<Integer, UserRoleEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(UserRoleEnum::getCode, Function.identity()));

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
     * 根据角色编码获取枚举对象
     *
     * @param code 角色编码
     * @return 对应的角色枚举，若无匹配则返回 null
     */
    @JsonCreator
    public static UserRoleEnum fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }
}
