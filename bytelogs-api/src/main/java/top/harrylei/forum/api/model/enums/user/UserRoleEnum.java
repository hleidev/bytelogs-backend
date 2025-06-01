package top.harrylei.forum.api.model.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 * 定义系统中的用户角色类型
 */
@Getter
@AllArgsConstructor
public enum UserRoleEnum {

    /**
     * 普通用户角色
     */
    NORMAL(0, "NORMAL"),
    
    /**
     * 管理员角色
     */
    ADMIN(1, "ADMIN");

    /**
     * 角色编码
     */
    private final Integer code;
    
    /**
     * 角色描述
     */
    private final String desc;
}
