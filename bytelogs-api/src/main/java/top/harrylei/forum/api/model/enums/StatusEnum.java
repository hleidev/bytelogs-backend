package top.harrylei.forum.api.model.enums;

import lombok.Getter;

/**
 * 全局状态码枚举类
 * <p>
 * 状态码设计规范：
 * 1. 使用五位数字编码：前两位表示模块，后三位表示具体错误
 * 2. 模块划分：
 *    - 00: 成功状态
 *    - 10: 通用错误
 *    - 20: 权限相关
 *    - 30: 资源相关
 *    - 40: 用户相关
 *    - 50: 文章相关
 *    - 60: 评论相关
 * 3. 每个状态码都有明确语义，避免重复或含义模糊
 */
@Getter
public enum StatusEnum {
    // ========== 成功状态 ==========
    SUCCESS(0, "OK"),

    // ========== 通用错误 10000-19999 ==========
    SYSTEM_ERROR(10000, "系统内部错误"),
    PARAM_ERROR(10001, "参数错误"),
    PARAM_MISSING(10002, "缺少必要参数: %s"),
    PARAM_TYPE_ERROR(10003, "参数类型错误: %s"),
    PARAM_VALIDATE_FAILED(10004, "参数校验失败: %s"),
    ILLEGAL_ARGUMENTS(10005, "参数异常"),
    ILLEGAL_ARGUMENTS_MIXED(10006, "参数异常: %s"),
    UNEXPECT_ERROR(10007, "非预期异常: %s"),
    REQUEST_BODY_ERROR(10008, "请求体格式错误或解析失败"),

    // ========== 权限相关 20000-29999 ==========
    UNAUTHORIZED(20001, "未登录"),
    FORBIDDEN(20002, "无权限"),
    FORBIDDEN_OPERATION(20003, "禁止的操作: %s"),
    FORBID_ERROR(20004, "无权限"),
    FORBID_ERROR_MIXED(20005, "无权限: %s"),
    FORBID_NOTLOGIN(20006, "未登录"),
    TOKEN_EXPIRED(20007, "登录已过期，请重新登录"),
    TOKEN_INVALID(20008, "无效的认证令牌"),

    // ========== 资源相关 30000-39999 ==========
    RESOURCE_NOT_FOUND(30001, "资源未找到: %s"),
    API_NOT_FOUND(30002, "接口不存在"),
    METHOD_NOT_ALLOWED(30003, "请求方法不支持"),
    RECORDS_NOT_EXISTS(30004, "记录不存在: %s"),
    RESOURCE_ALREADY_EXISTS(30005, "资源已存在: %s"),

    // ========== 用户相关 40000-49999 ==========
    USER_NOT_EXISTS(40001, "用户不存在: %s"),
    USER_INFO_NOT_EXISTS(40002, "用户信息不存在"),
    USER_EXISTS(40003, "用户已存在: %s"),
    USER_LOGIN_NAME_DUPLICATE(40004, "用户登录名重复: %s"),
    USER_PASSWORD_INVALID(40005, "密码不符合规范，需包含字母、数字、特殊字符，长度为 8-20 位"),
    USER_LOGIN_FAILED(40006, "登录失败: %s"),
    USER_USERNAME_OR_PASSWORD_ERROR(40007, "用户名或密码错误"),
    USER_PASSWORD_ERROR(400008, "密码错误"),
    USER_LOCKED(40009, "用户已被锁定"),
    USER_DISABLED(40010, "账号已被禁用"),
    USER_NOT_ACTIVATED(40011, "账号未激活"),
    USER_UPDATE_FAILED(40012, "更新失败: %s"),
    USER_DELETE_FAILED(40013, "删除用户失败"),
    USER_RESTORE_FAILED(40014, "恢复用户失败"),

    // ========== 文章相关 50000-59999 ==========
    ARTICLE_NOT_EXISTS(50001, "文章不存在: %s"),
    COLUMN_NOT_EXISTS(50002, "教程不存在: %s"),
    ARTICLE_PUBLISH_FAILED(50003, "文章发布失败: %s"),
    ARTICLE_ALREADY_EXISTS(50004, "文章已存在: %s"),
    ARTICLE_CONTENT_INVALID(50005, "文章内容不符合规范: %s"),

    // ========== 评论相关 60000-69999 ==========
    COMMENT_NOT_EXISTS(60001, "评论不存在: %s"),
    COMMENT_CONTENT_INVALID(60002, "评论内容不符合规范: %s"),
    COMMENT_DISABLED(60003, "评论功能已关闭");

    /**
     * 业务状态码
     */
    private final int code;

    /**
     * 状态消息
     */
    private final String message;

    /**
     * 构造状态枚举对象
     *
     * @param code 状态码
     * @param message 状态消息
     */
    StatusEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 是否为成功状态
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }

    /**
     * 获取对应的HTTP状态码
     */
    public int getHttpStatus() {
        if (code >= 40000 && code < 50000) {
            return 400; // 客户端错误
        } else if (code >= 20000 && code < 30000) {
            return 403; // 权限错误
        } else if (code >= 30000 && code < 40000) {
            return 404; // 资源不存在
        } else if (code >= 10000) {
            return 500; // 服务器错误
        }
        return 200; // 成功
    }

    /**
     * 根据状态码获取状态枚举
     *
     * @param code 状态码
     * @return 状态枚举，不存在时返回 null
     */
    public static StatusEnum getByCode(int code) {
        for (StatusEnum statusEnum : values()) {
            if (statusEnum.getCode() == code) {
                return statusEnum;
            }
        }
        return null;
    }
}
