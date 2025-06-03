package top.harrylei.forum.web.user;

import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.StatusEnum;
import top.harrylei.forum.api.model.vo.ResVO;
import top.harrylei.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import top.harrylei.forum.api.model.vo.user.req.PasswordUpdateReq;
import top.harrylei.forum.api.model.vo.user.req.UserInfoReq;
import top.harrylei.forum.api.model.vo.user.vo.UserInfoVO;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.service.user.converted.UserInfoStructMapper;
import top.harrylei.forum.service.user.service.UserService;
import top.harrylei.forum.service.util.JwtUtil;
import top.harrylei.forum.web.security.permission.RequiresLogin;

/**
 * 用户控制器
 * <p>
 * 处理用户信息查询、修改等相关请求
 */
@Tag(name = "用户相关模块", description = "提供查询、修改信息、修改密码等接口")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Validated
@RequiresLogin
public class UserController {

    private final UserService userService;
    private final UserInfoStructMapper userInfoStructMapper;
    private final JwtUtil jwtUtil;

    /**
     * 获取当前登录用户的个人信息
     * 
     * @return 包含用户信息的视图对象的响应
     */
    @Operation(summary = "获取个人信息", description = "获取当前登录用户的个人基本信息")
    @GetMapping("/info")
    public ResVO<UserInfoVO> getUserInfo() {
        // 从请求上下文中获取当前用户信息
        BaseUserInfoDTO user = ReqInfoContext.getContext().getUser();
        log.info("获取个人信息成功: userId={}", user.getUserId());
        // 将用户DTO转换为前端展示所需的VO对象
        return ResVO.ok(userInfoStructMapper.toVO(user));
    }

    /**
     * 更新当前登录用户的个人信息
     * 
     * @param userInfoReq 用户信息更新请求，包含需要更新的字段
     * @return 操作成功的响应
     */
    @Operation(summary = "修改个人信息", description = "更新当前登录用户的个人基本信息")
    @PostMapping("/update")
    public ResVO<Void> updateUserInfo(@Valid @RequestBody UserInfoReq userInfoReq) {
        // 获取当前用户信息并更新
        BaseUserInfoDTO oldUserInfo = ReqInfoContext.getContext().getUser();
        BaseUserInfoDTO newUserInfo = new BaseUserInfoDTO();
        BeanUtils.copyProperties(oldUserInfo, newUserInfo);
        userInfoStructMapper.updateDTOFromReq(userInfoReq, newUserInfo);

        // 调用服务层处理更新逻辑
        userService.updateUserInfo(newUserInfo);
        log.info("修改个人信息成功: userId={}", oldUserInfo.getUserId());
        return ResVO.ok();
    }

    /**
     * 更新当前登录用户的个人密码
     *
     * @param passwordUpdateReq 用户密码更新请求
     * @return 操作成功响应
     */
    @Operation(summary = "修改用户密码", description = "更新当前登录用户的个人密码")
    @PostMapping("/password/update")
    public ResVO<Void> updatePassword(@RequestHeader(name = "Authorization", required = false) String authHeader,
        @Valid @RequestBody PasswordUpdateReq passwordUpdateReq) {
        String token = jwtUtil.extractTokenFromAuthorizationHeader(authHeader);
        ExceptionUtil.requireNonEmpty(token, StatusEnum.USER_UPDATE_FAILED, "注销请求缺少有效的Authorization头");

        Long userId = ReqInfoContext.getContext().getUserId();

        userService.updatePassword(token, passwordUpdateReq.getOldPassword(), passwordUpdateReq.getNewPassword());
        log.info("密码修改成功: userId={}", userId);

        return ResVO.ok();
    }

    /**
     * 更新当前登录用户的个人头像
     * 
     * @param avatar 用户头像
     * @return 操作成功
     */
    @Operation(summary = "修改用户头像", description = "更新当前登录用户的个人头像")
    @PostMapping("/avatar/update")
    public ResVO<Void> updateAvatar(@NotBlank(message = "用户头像不能为空") String avatar) {
        userService.updateAvatar(avatar);
        log.info("修改头像成功: userId={}", ReqInfoContext.getContext().getUser().getUserId());

        return ResVO.ok();
    }
}
