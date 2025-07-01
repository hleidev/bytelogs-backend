package top.harrylei.forum.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import top.harrylei.forum.api.validation.SecureContent.ContentSecurityType;

import java.util.regex.Pattern;

/**
 * 简化的安全验证器
 *
 * @author harry
 */
public class SecureContentConstraintValidator implements ConstraintValidator<SecureContent, String> {

    /**
     * XSS威胁检测模式
     */
    private static final Pattern[] XSS_PATTERNS = {
        // 脚本标签
        Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        // 伪协议
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("data:text/html", Pattern.CASE_INSENSITIVE),
        // 事件处理器
        Pattern.compile("onload\\s*=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("onerror\\s*=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("onclick\\s*=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("onmouseover\\s*=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("onfocus\\s*=", Pattern.CASE_INSENSITIVE),
        // 危险标签
        Pattern.compile("<iframe[^>]*>.*?</iframe>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("<object[^>]*>.*?</object>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("<embed[^>]*>", Pattern.CASE_INSENSITIVE),
        // 表达式注入
        Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("url\\s*\\(\\s*javascript:", Pattern.CASE_INSENSITIVE)
    };

    private ContentSecurityType contentType;
    private boolean allowEmpty;

    @Override
    public void initialize(SecureContent constraintAnnotation) {
        this.contentType = constraintAnnotation.contentType();
        this.allowEmpty = constraintAnnotation.allowEmpty();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 空值处理
        if (StringUtils.isBlank(value)) {
            return allowEmpty;
        }

        // 长度检查
        if (value.length() > getMaxLength(contentType)) {
            setCustomMessage(context, "内容长度超出限制");
            return false;
        }

        // 仅对非Markdown内容进行XSS检测
        if (contentType != ContentSecurityType.MARKDOWN) {
            if (containsXssThreats(value)) {
                setCustomMessage(context, "内容包含潜在安全威胁");
                return false;
            }
        }

        return true;
    }

    /**
     * 检测XSS威胁
     */
    private boolean containsXssThreats(String content) {
        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(content).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取最大长度限制
     */
    private int getMaxLength(ContentSecurityType contentType) {
        return switch (contentType) {
            case MARKDOWN -> 100000;
            case CODE -> 50000;
            case HTML -> 20000;
            default -> 10000;
        };
    }

    /**
     * 设置自定义错误消息
     */
    private void setCustomMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
               .addConstraintViolation();
    }
}