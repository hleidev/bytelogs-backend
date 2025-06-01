package top.harrylei.forum.web.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.vo.ResVO;
import top.harrylei.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import top.harrylei.forum.api.model.vo.user.vo.UserInfoVO;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.service.user.converted.UserInfoStructMapper;
import top.harrylei.forum.web.security.permission.RequiresLogin;

/**
 * 用户控制器
 * 处理用户信息查询、修改等相关请求
 */
@Tag(name = "用户相关模块", description = "提供查询、修改信息、修改密码等接口")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserInfoStructMapper userInfoStructMapper;

    /**
     * 获取当前登录用户的个人信息
     * 
     * @return 用户信息视图对象
     */
    @GetMapping("/info")
    @RequiresLogin
    public ResVO<UserInfoVO> getUserInfo() {
        // 从请求上下文中获取当前用户信息
        BaseUserInfoDTO user = ReqInfoContext.getContext().getUser();
        // 将用户DTO转换为前端展示所需的VO对象
        return ResVO.ok(userInfoStructMapper.toVO(user));
    }
}
