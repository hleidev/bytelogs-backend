package top.harrylei.community.api.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录类型枚举
 *
 * @author harry
 */
@Getter
@AllArgsConstructor
public enum LoginTypeEnum {
    /**
     * 用户名+密码登录
     */
    USERNAME_PASSWORD(0),
    /**
     * 邮箱密码登录
     */
    EMAIL_PASSWORD(1);

    private final int code;
}
