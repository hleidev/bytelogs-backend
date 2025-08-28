package top.harrylei.community.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 安全内容验证注解
 *
 * @author harry
 */
@Documented
@Constraint(validatedBy = {SecureContentConstraintValidator.class})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SecureContent {

    String message() default "内容包含潜在安全威胁";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 内容安全类型
     */
    ContentSecurityType contentType() default ContentSecurityType.PLAIN_TEXT;

    /**
     * 是否允许空内容
     */
    boolean allowEmpty() default true;

    /**
     * 内容安全类型枚举
     */
    enum ContentSecurityType {
        PLAIN_TEXT,
        MARKDOWN,
        HTML,
        CODE
    }
}