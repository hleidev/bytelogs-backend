package top.harrylei.forum.web.admin;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.StatusEnum;
import top.harrylei.forum.api.model.enums.user.UserRoleEnum;
import top.harrylei.forum.api.model.enums.user.UserStatusEnum;
import top.harrylei.forum.api.model.vo.ResVO;
import top.harrylei.forum.api.model.vo.auth.UserCreateReq;
import top.harrylei.forum.api.model.vo.page.PageHelper;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.api.model.vo.page.param.UserQueryParam;
import top.harrylei.forum.api.model.vo.user.dto.UserDetailDTO;
import top.harrylei.forum.api.model.vo.user.vo.UserDetailVO;
import top.harrylei.forum.api.model.vo.user.vo.UserListItemVO;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.core.security.permission.RequiresAdmin;
import top.harrylei.forum.service.admin.service.UserManagementService;
import top.harrylei.forum.service.user.converted.UserStructMapper;

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
    private final UserStructMapper userStructMapper;

    /**
     * 分页查询用户列表
     *
     * @param queryParam 查询参数，包括页码、页大小、排序字段及过滤条件
     * @return 用户列表的分页响应对象
     */
    @Operation(summary = "查询用户列表", description = "分页查询用户列表，支持多条件筛选和多字段排序")
    @GetMapping
    public ResVO<PageVO<UserListItemVO>> list(UserQueryParam queryParam) {
        PageVO<UserDetailDTO> pageVO = userManagementService.list(queryParam);
        return ResVO.ok(PageHelper.map(pageVO, userStructMapper::toUserDetailVO));
    }

    /**
     * 获取用户详情
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    @GetMapping("/{userId}")
    public ResVO<UserDetailVO> getUserDetail(@NotNull(message = "用户ID为空") @PathVariable Long userId) {
        UserDetailDTO UserDetailDTO = userManagementService.getUserDetail(userId);
        return ResVO.ok(userStructMapper.toUserDetailVO(UserDetailDTO));
    }

    /**
     * 修改用户状态
     *
     * @param userId 用户ID
     * @param status 新状态
     * @return 操作结果
     */
    @Operation(summary = "修改用户状态", description = "启用或禁用指定用户")
    @PutMapping("/{userId}/status")
    public ResVO<Void> updateStatus(@NotNull(message = "用户ID为空") @PathVariable Long userId,
        @NotNull(message = "状态为空") @RequestBody Integer status) {
        UserStatusEnum statusEnum = UserStatusEnum.fromCode(status);
        ExceptionUtil.requireNonNull(statusEnum, StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "非法用户状态 statusCode=" + status);
        userManagementService.updateStatus(userId, statusEnum);
        return ResVO.ok();
    }

    /**
     * 重置用户密码
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @Operation(summary = "重置用户密码", description = "将用户密码重置为系统默认密码并通知用户")
    @PutMapping("/{userId}/password/reset")
    public ResVO<Void> resetPassword(@NotNull(message = "用户ID为空") @PathVariable Long userId) {
        userManagementService.resetPassword(userId);
        return ResVO.ok();
    }

    /**
     * 修改用户邮箱
     *
     * @param userId 用户ID
     * @param email 新邮箱地址
     * @return 操作结果
     */
    @Operation(summary = "修改用户邮箱", description = "更新用户的邮箱地址")
    @PutMapping("/{userId}/email")
    public ResVO<Void> updateEmail(@NotNull(message = "用户ID为空") @PathVariable Long userId,
        @NotBlank(message = "邮箱为空") @RequestBody String email) {
        // TODO userManagementService.updateEmail(userId, email);
        return ResVO.ok();
    }

    /**
     * 删除用户账户
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @Operation(summary = "删除用户账户", description = "将用户标记为已删除状态")
    @DeleteMapping("/{userId}")
    public ResVO<Void> deleteUser(@NotNull(message = "用户ID为空") @PathVariable Long userId) {
        userManagementService.delete(userId);
        return ResVO.ok();
    }

    /**
     * 恢复用户账户
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @Operation(summary = "恢复用户账户", description = "将用户标记为未删除状态")
    @PostMapping("/{userId}/restore")
    public ResVO<Void> restoreUser(@NotNull(message = "用户ID为空") @PathVariable Long userId) {
        userManagementService.restore(userId);
        return ResVO.ok();
    }

    /**
     * 修改用户角色
     *
     * @param userId 用户ID
     * @param roleCode 角色编码
     * @return 操作结果
     */
    @Operation(summary = "修改用户角色", description = "修改用户的系统角色")
    @PutMapping("/{userId}/role")
    public ResVO<Void> updateUserRole(@NotNull(message = "用户ID为空") @PathVariable Long userId,
        @NotNull(message = "角色编码为空") @RequestBody Integer roleCode) {
        userManagementService.updateUserRole(userId, UserRoleEnum.fromCode(roleCode));
        return ResVO.ok();
    }

    /**
     * 新建用户账号
     *
     * @param req 新建用户的请求参数
     * @return 操作结果
     */
    @Operation(summary = "新建用户账号", description = "后台管理端新建用户")
    @PostMapping
    public ResVO<Void> saveUser(@Valid @RequestBody UserCreateReq req) {
        userManagementService.save(req);
        return ResVO.ok();
    }
}
