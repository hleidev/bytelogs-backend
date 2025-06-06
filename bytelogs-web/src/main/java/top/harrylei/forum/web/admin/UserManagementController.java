package top.harrylei.forum.web.admin;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.vo.ResVO;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.api.model.vo.page.param.UserQueryParam;
import top.harrylei.forum.api.model.vo.user.vo.UserListItemVO;
import top.harrylei.forum.service.admin.service.UserManagementService;
import top.harrylei.forum.web.security.permission.RequiresAdmin;

/**
 * 用户管理模块
 */
@Tag(name = "用户管理模块", description = "提供用户查询、创建、状态管理等功能")
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiresAdmin
@RequiredArgsConstructor
@Validated
public class UserManagementController {

    private final UserManagementService userManagementService;

    /**
     * 分页查询用户列表
     *
     * @param queryParam 查询参数，包括页码、页大小、排序字段及过滤条件
     * @return 用户列表的分页响应对象
     */
    @Operation(summary = "查询用户列表", description = "分页查询用户列表，支持多条件筛选和多字段排序")
    @GetMapping
    public ResVO<PageVO<UserListItemVO>> list(UserQueryParam queryParam) {
        PageVO<UserListItemVO> pageVO = userManagementService.list(queryParam);
        return ResVO.ok(pageVO);
    }
}
