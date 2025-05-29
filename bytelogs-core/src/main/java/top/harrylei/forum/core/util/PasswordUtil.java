package top.harrylei.forum.core.util;

import java.util.regex.Pattern;

public class PasswordUtil {

    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d])[A-Za-z\\d\\S]{8,20}$");

    /**
     * 密码强度校验：8-20 位，包含字母、数字、特殊字符
     */
    public static boolean isValid(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    // TODO 将来可加入复杂度评分、密码清洗等功能
}
