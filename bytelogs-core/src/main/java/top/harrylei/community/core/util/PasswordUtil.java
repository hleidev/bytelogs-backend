package top.harrylei.community.core.util;

import java.util.regex.Pattern;

/**
 * 密码工具类
 *
 * @author harry
 */
public class PasswordUtil {

    /**
     * 密码校验规则：8-20位，包含字母和数字，可包含特殊字符
     * 与AuthReq保持一致
     */
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[a-zA-Z0-9_@#%&!$*-]{8,20}$");


    /**
     * 密码强度校验：8-20 位，包含字母、数字、特殊字符
     *
     * @param password 密码
     * @return 是否符合规则
     */
    public static boolean isValid(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * 密码强度校验：8-20 位，包含字母、数字、特殊字符
     *
     * @param password 密码
     * @return 是否不符合规则
     */
    public static boolean isInvalid(String password) {
        return !isValid(password);
    }

    // TODO 将来可加入复杂度评分、密码清洗等功能
}
