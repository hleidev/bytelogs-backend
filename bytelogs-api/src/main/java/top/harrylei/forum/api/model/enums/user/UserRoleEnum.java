package top.harrylei.forum.api.model.enums.user;

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
 * 用户角色枚举
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
    private final Integer code;

    // 角色描述（用于展示）
    private final String label;

    // 根据角色编码快速定位枚举实例
    private static final Map<Integer, UserRoleEnum> CODE_MAP =
        Arrays.stream(values()).collect(Collectors.toMap(UserRoleEnum::getCode, Function.identity()));
    // 根据枚举名称（不区分大小写）快速定位枚举实例
    private static final Map<String, UserRoleEnum> NAME_MAP =
        Arrays.stream(values()).collect(Collectors.toMap(e -> e.name().toUpperCase(), Function.identity()));

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

    /**
     * 根据角色名称获取枚举对象（忽略大小写）
     *
     * @param name 枚举名称
     * @return 对应的角色枚举，若无匹配或为空则返回 null
     */
    public static UserRoleEnum fromName(String name) {
        if (StringUtils.isBlank(name))
            return null;
        return NAME_MAP.get(name.toUpperCase());
    }

    /**
     * 根据角色编码获取角色描述
     *
     * @param code 角色编码
     * @return 角色描述，若无匹配则返回 null
     */
    public static String getLabelByCode(Integer code) {
        UserRoleEnum role = fromCode(code);
        return role == null ? null : role.getLabel();
    }

    /**
     * 根据角色代码获取角色标签，若未找到则返回默认值
     * 
     * @param code 角色编码
     * @param defaultLabel 默认值
     * @return 角色描述，若无匹配则返回 默认值
     */
    public static String getLabelByCode(Integer code, String defaultLabel) {
        UserRoleEnum role = fromCode(code);
        return role == null ? defaultLabel : role.getLabel();
    }

    /**
     * 根据角色名称获取角色编码
     *
     * @param name 角色名称
     * @return 角色编码，若无匹配则返回 null
     */
    public static Integer getCodeByName(String name) {
        UserRoleEnum role = fromName(name);
        return role == null ? null : role.getCode();
    }

    /**
     * 根据角色编码获取角色名称（枚举名）
     *
     * @param code 角色编码
     * @return 角色名称，若无匹配则返回 NORMAL
     */
    public static String getNameByCode(Integer code) {
        UserRoleEnum role = fromCode(code);
        return role == null ? NORMAL.name() : role.name();
    }

    /**
     * 根据角色编码获取角色名称（枚举名）
     *
     * @param code 角色编码
     * @param defaultName 默认角色名
     * @return 角色名称，若无匹配则返回默认值
     */
    public static String getNameByCode(Integer code, String defaultName) {
        UserRoleEnum role = fromCode(code);
        return role == null ? defaultName : role.name();
    }
}
