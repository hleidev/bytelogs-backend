package top.harrylei.forum.api.model.vo.constants;

import lombok.Getter;

/**
 * 全局状态码枚举类
 *
 * 定义系统中所有接口返回的状态码及其对应信息，遵循以下命名规范：
 * 格式：业务码_状态码_业务子码（共9位）
 * - 业务码：如100表示全局模块，200表示文章模块
 * - 状态码：参考HTTP状态语义，如400参数错误，403权限问题，404资源不存在，500服务异常
 * - 业务子码：具体细分的业务错误编号
 *
 * 使用方式： 统一通过 StatusEnum 传递错误状态，便于异常处理和接口返回一致性
 */
@Getter
public enum StatusEnum {
    // ========== 通用成功状态 ==========
    SUCCESS(0, "OK"),

    // ========== 全局错误码：100 开头 ==========
    // 参数错误相关：400 开头
    ILLEGAL_ARGUMENTS(100_400_001, "参数异常"),
    ILLEGAL_ARGUMENTS_MIXED(100_400_002, "参数异常:%s"),
    PARAM_MISSING(100_400_003, "缺少必要参数:%s"),
    PARAM_TYPE_ERROR(100_400_004, "参数类型错误:%s"),
    PARAM_VALIDATE_FAILED(100_400_005, "参数校验失败:%s"),

    // 权限错误相关：403 开头
    FORBID_ERROR(100_403_001, "无权限"),
    FORBID_ERROR_MIXED(100_403_002, "无权限:%s"),
    FORBID_NOTLOGIN(100_403_003, "未登录"),
    FORBIDDEN_OPERATION(100_403_004, "禁止的操作"),

    // 资源不存在相关：404 开头
    RECORDS_NOT_EXISTS(100_404_001, "记录不存在:%s"),
    RESOURCE_NOT_FOUND(100_404_002, "资源未找到:%s"),
    API_NOT_FOUND(100_404_003, "接口不存在"),

    // 请求方法错误：405 开头
    METHOD_NOT_ALLOWED(100_405_001, "请求方法不支持"),

    // 系统内部错误相关：500 开头
    UNEXPECT_ERROR(100_500_001, "非预期异常:%s"),
    SYSTEM_ERROR(100_500_002, "系统内部错误"),
    DB_ERROR(100_500_003, "数据库操作异常"),
    UPLOAD_PIC_FAILED(100_500_004, "图片上传失败"),

    // ========== 文章模块错误码：200 开头 ==========
    ARTICLE_NOT_EXISTS(200_404_001, "文章不存在:%s"),
    COLUMN_NOT_EXISTS(200_404_002, "教程不存在:%s"),
    COLUMN_QUERY_ERROR(200_500_003, "教程查询异常:%s"),
    COLUMN_ARTICLE_EXISTS(200_500_004, "专栏教程已存在:%s"),
    ARTICLE_RELATION_TUTORIAL(200_500_006, "文章已被添加为教程:%s"),

    // ========== 评论模块错误码：300 开头 ==========
    COMMENT_NOT_EXISTS(300_404_001, "评论不存在:%s"),

    // ========== 用户模块错误码：400 开头 ==========
    LOGIN_FAILED_MIXED(400_403_001, "登录失败:%s"),
    USER_NOT_EXISTS(400_404_001, "用户不存在:%s"),
    USER_EXISTS(400_404_002, "用户已存在:%s"),
    USER_NAME_OR_PASSWORD_EMPTY(400_400_001, "用户名或密码不能为空"),
    USER_LOGIN_NAME_REPEAT(400_404_003, "用户登录名重复:%s"),
    USER_PASSWORD_INVALID(400_422_001, "密码不符合规范，需包含字母、数字、特殊字符，长度为 8-20 位"),
    USER_NOT_AUDIT(400_500_001, "用户未审核:%s"),
    USER_PWD_ERROR(400_500_002, "用户名或密码错误");

    /**
     * 模块代码掩码：获取模块业务码
     */
    private static final int MODULE_MASK = 1000_000_000;
    
    /**
     * HTTP状态码掩码：获取HTTP状态码
     */
    private static final int HTTP_MASK = 1000_000;
    
    /**
     * 业务子码掩码：获取业务子码
     */
    private static final int SUB_MASK = 1000;

    /**
     * 状态码（九位整数，含业务码、状态码、业务子码）
     */
    private final int code;

    /**
     * 状态对应的提示信息（支持占位符格式化）
     */
    private final String msg;

    /**
     * 构造状态枚举对象
     *
     * @param code 状态码
     * @param msg 状态信息
     */
    StatusEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 获取模块业务码
     *
     * @return 模块业务码
     */
    public int getModuleCode() {
        return code / MODULE_MASK;
    }
    
    /**
     * 获取HTTP状态码
     *
     * @return HTTP状态码
     */
    public int getHttpStatus() {
        return (code % MODULE_MASK) / HTTP_MASK;
    }
    
    /**
     * 获取业务子码
     *
     * @return 业务子码
     */
    public int getSubCode() {
        return code % HTTP_MASK;
    }

    /**
     * 判断是否为 5xx 服务内部异常类错误
     *
     * @param code 状态码
     * @return 是否为服务内部异常
     */
    public static boolean is5xx(int code) {
        return (code % MODULE_MASK) / HTTP_MASK >= 500;
    }

    /**
     * 判断是否为 403 权限类错误
     *
     * @param code 状态码
     * @return 是否为无权限错误
     */
    public static boolean is403(int code) {
        return (code % MODULE_MASK) / HTTP_MASK == 403;
    }

    /**
     * 判断是否为 4xx 客户端参数类错误
     *
     * @param code 状态码
     * @return 是否为参数相关错误
     */
    public static boolean is4xx(int code) {
        return (code % MODULE_MASK) / HTTP_MASK < 500;
    }
}
