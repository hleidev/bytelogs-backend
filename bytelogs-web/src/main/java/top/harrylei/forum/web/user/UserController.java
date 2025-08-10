package top.harrylei.forum.web.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.harrylei.forum.api.enums.ErrorCodeEnum;
import top.harrylei.forum.api.enums.ResultCode;
import top.harrylei.forum.api.model.base.ResVO;
import top.harrylei.forum.api.model.page.PageVO;
import top.harrylei.forum.api.model.user.dto.UserInfoDTO;
import top.harrylei.forum.api.model.user.req.PasswordUpdateReq;
import top.harrylei.forum.api.model.user.req.UserFollowQueryParam;
import top.harrylei.forum.api.model.user.req.UserInfoUpdateReq;
import top.harrylei.forum.api.model.user.vo.UserFollowVO;
import top.harrylei.forum.api.model.user.vo.UserInfoVO;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.core.security.permission.RequiresLogin;
import top.harrylei.forum.service.user.converted.UserStructMapper;
import top.harrylei.forum.service.user.service.UserFollowService;
import top.harrylei.forum.service.user.service.UserService;

/**
 * 用户控制器
 *
 * @author harry
 */
@Tag(name = "用户相关模块", description = "提供查询、修改信息、修改密码等接口")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/user")
@Validated
@RequiresLogin
public class UserController {

    private final UserService userService;
    private final UserFollowService userFollowService;
    private final UserStructMapper userStructMapper;

    /**
     * 获取当前登录用户的个人信息
     *
     * @return 包含用户信息的视图对象的响应
     */
    @Operation(summary = "查询用户信息", description = "获取当前登录用户的个人基本信息")
    @GetMapping("/profile")
    public ResVO<UserInfoVO> getUserInfo() {
        UserInfoDTO userInfo = ReqInfoContext.getContext().getUser();
        if (userInfo == null) {
            ResultCode.INTERNAL_ERROR.throwException();
        }
        return ResVO.ok(userStructMapper.toVO(userInfo));
    }

    /**
     * 更新当前登录用户的个人信息
     *
     * @param userInfoUpdateReq 用户信息更新请求，包含需要更新的字段
     * @return 操作成功的响应
     */
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的个人基本信息")
    @PutMapping("/info")
    public ResVO<Void> updateUserInfo(@Valid @RequestBody UserInfoUpdateReq userInfoUpdateReq) {
        // 获取当前上下文中的用户信息
        UserInfoDTO userInfo = ReqInfoContext.getContext().getUser();
        if (userInfo == null) {
            ResultCode.INTERNAL_ERROR.throwException();
        }

        // 直接更新上下文中的用户信息
        userStructMapper.applyUserInfoUpdates(userInfoUpdateReq, userInfo);

        // 调用服务层处理更新逻辑
        userService.updateUserInfo(userInfo);

        return ResVO.ok();
    }

    /**
     * 修改当前登录用户的个人密码
     *
     * @param passwordUpdateReq 用户密码更新请求
     * @return 操作成功响应
     */
    @Operation(summary = "修改用户密码", description = "修改当前登录用户的个人密码")
    @PutMapping("/password")
    public ResVO<Void> updatePassword(@Valid @RequestBody PasswordUpdateReq passwordUpdateReq) {
        Long userId = ReqInfoContext.getContext().getUserId();
        ExceptionUtil.requireValid(userId, ErrorCodeEnum.PARAM_VALIDATE_FAILED, "用户ID为空");
        userService.updatePassword(userId, passwordUpdateReq.getOldPassword(), passwordUpdateReq.getNewPassword());
        return ResVO.ok();
    }

    /**
     * 修改当前登录用户的个人头像
     *
     * @param avatar 用户头像
     * @return 操作成功
     */
    @Operation(summary = "修改用户头像", description = "修改当前登录用户的个人头像")
    @PutMapping("/avatar")
    public ResVO<Void> updateAvatar(@NotBlank(message = "用户头像不能为空") String avatar) {
        Long userId = ReqInfoContext.getContext().getUserId();
        userService.updateAvatar(userId, avatar);
        return ResVO.ok();
    }

    /**
     * 关注用户
     *
     * @param followUserId 要关注的用户ID
     * @return 操作结果
     */
    @Operation(summary = "关注用户", description = "关注指定用户")
    @PostMapping("/{followUserId}/follow")
    public ResVO<Void> follow(@NotNull(message = "用户ID不能为空") @PathVariable Long followUserId) {
        userFollowService.followUser(followUserId);
        return ResVO.ok();
    }

    /**
     * 取消关注用户
     *
     * @param followUserId 要取消关注的用户ID
     * @return 操作结果
     */
    @Operation(summary = "取消关注用户", description = "取消关注指定用户")
    @DeleteMapping("/{followUserId}/follow")
    public ResVO<Void> unfollow(@PathVariable Long followUserId) {
        userFollowService.unfollowUser(followUserId);
        return ResVO.ok();
    }

    /**
     * 获取用户关注列表
     *
     * @param queryParam 查询参数
     * @return 关注列表
     */
    @Operation(summary = "获取关注列表", description = "分页获取用户的关注列表，userId为空时查询当前用户")
    @GetMapping("/following")
    public ResVO<PageVO<UserFollowVO>> getFollowingList(@Valid UserFollowQueryParam queryParam) {
        // 如果没传userId，使用当前用户ID
        if (queryParam.getUserId() == null) {
            queryParam.setUserId(ReqInfoContext.getContext().getUserId());
        }

        PageVO<UserFollowVO> followingList = userFollowService.pageFollowingList(queryParam);
        return ResVO.ok(followingList);
    }

    /**
     * 获取用户粉丝列表
     *
     * @param queryParam 查询参数
     * @return 粉丝列表
     */
    @Operation(summary = "获取粉丝列表", description = "分页获取用户的粉丝列表，userId为空时查询当前用户")
    @GetMapping("/followers")
    public ResVO<PageVO<UserFollowVO>> getFollowersList(@Valid UserFollowQueryParam queryParam) {
        if (queryParam.getFollowUserId() == null) {
            queryParam.setFollowUserId(ReqInfoContext.getContext().getUserId());
        }

        PageVO<UserFollowVO> followersList = userFollowService.pageFollowersList(queryParam);
        return ResVO.ok(followersList);
    }
}
