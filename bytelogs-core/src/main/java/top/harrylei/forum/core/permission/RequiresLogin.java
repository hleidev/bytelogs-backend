package top.harrylei.forum.core.permission;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

/**
 * 登录权限注解
 * <p>
 * 用于标记需要用户登录才能访问的方法或类。
 * 在方法或类上使用此注解后，只有已登录的用户才能访问，未登录用户将被拒绝。
 * 基于Spring Security的方法级权限控制实现。
 * </p>
 * 
 * 用法示例:
 * <pre>
 * {@code
 * @RequiresLogin
 * public void someUserOnlyMethod() {
 *     // 只有登录用户可以执行的方法
 * }
 * }
 * </pre>
 * 
 * @see org.springframework.security.access.prepost.PreAuthorize
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@PreAuthorize("isAuthenticated()")
public @interface RequiresLogin {
} 