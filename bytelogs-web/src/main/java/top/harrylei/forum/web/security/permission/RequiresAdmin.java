package top.harrylei.forum.web.security.permission;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

/**
 * 管理员权限注解
 * <p>
 * 用于标记需要管理员权限才能访问的方法或类。
 * 在方法或类上使用此注解后，只有具有ADMIN角色的用户才能访问。
 * 基于Spring Security的方法级权限控制实现。
 * </p>
 * 
 * 用法示例:
 * <pre>
 * {@code
 * @RequiresAdmin
 * public void someAdminOnlyMethod() {
 *     // 只有管理员可以执行的方法
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
@PreAuthorize("hasRole('ADMIN')")
public @interface RequiresAdmin {
} 