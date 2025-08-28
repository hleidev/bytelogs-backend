package top.harrylei.community.core.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt密码加密工具类
 *
 * @author harry
 */
public class BCryptUtil {
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 加密密码（内部含随机盐）
     *
     * @param plainPassword 明文密码
     * @return 加密后的密码
     */
    public static String encode(String plainPassword) {
        return ENCODER.encode(plainPassword);
    }

    /**
     * 校验密码
     *
     * @param plainPassword  明文密码
     * @param hashedPassword 加密密码
     * @return 是否匹配
     */
    public static boolean matches(String plainPassword, String hashedPassword) {
        return ENCODER.matches(plainPassword, hashedPassword);
    }

    /**
     * 校验密码不匹配
     *
     * @param plainPassword  明文密码
     * @param hashedPassword 加密密码
     * @return 是否不匹配
     */
    public static boolean notMatches(String plainPassword, String hashedPassword) {
        return !matches(plainPassword, hashedPassword);
    }
}
