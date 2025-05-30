package top.harrylei.forum.api.model.enums;

import lombok.Getter;

/**
 * 用户角色枚举类
 */
@Getter
public enum RoleEnum {
    NORMAL(0, "普通用户"), ADMIN(1, "超级用户");

    private final int code;
    private final String desc;

    RoleEnum(int role, String desc) {
        this.code = role;
        this.desc = desc;
    }

    public static String of(Integer code) {
        for (RoleEnum role : values()) {
            if (role.getCode() == code) {
                return role.name();
            }
        }
        return NORMAL.name();
    }
}
