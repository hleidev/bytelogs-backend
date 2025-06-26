package top.harrylei.forum.web.admin;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.harrylei.forum.core.security.permission.RequiresAdmin;

/**
 * 评论管理控制器
 *
 * @author harry
 */
@Tag(name = "评论管理模块", description = "提供评论后台管理接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/article")
@RequiredArgsConstructor
@RequiresAdmin
@Validated
public class CommentManagementController {
}