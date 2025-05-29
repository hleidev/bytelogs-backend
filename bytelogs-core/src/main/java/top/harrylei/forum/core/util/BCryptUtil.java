package top.harrylei.forum.core.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptUtil {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // 加密密码（内部含随机盐）
    public static String hash(String plainPassword) {
        return encoder.encode(plainPassword);
    }

    // 校验密码
    public static boolean matches(String plainPassword, String hashedPassword) {
        return encoder.matches(plainPassword, hashedPassword);
    }
}
