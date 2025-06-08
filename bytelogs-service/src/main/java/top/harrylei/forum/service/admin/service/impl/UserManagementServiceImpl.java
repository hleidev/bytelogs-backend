package top.harrylei.forum.service.admin.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.StatusEnum;
import top.harrylei.forum.api.model.enums.user.UserStatusEnum;
import top.harrylei.forum.api.model.vo.page.PageHelper;
import top.harrylei.forum.api.model.vo.page.PageReq;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.api.model.vo.page.param.UserQueryParam;
import top.harrylei.forum.api.model.vo.user.dto.UserDetailDTO;
import top.harrylei.forum.api.model.vo.user.vo.UserListItemVO;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.service.admin.service.UserManagementService;
import top.harrylei.forum.service.user.converted.UserStructMapper;
import top.harrylei.forum.service.user.service.UserService;

/**
 * 用户管理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserService userService;
    private final UserStructMapper userStructMapper;

    /**
     * 分页查询用户列表
     *
     * @param queryParam 查询条件
     * @return 用户分页列表
     */
    @Override
    public PageVO<UserListItemVO> list(UserQueryParam queryParam) {
        // 参数校验
        ExceptionUtil.requireNonNull(queryParam, StatusEnum.PARAM_MISSING, "查询参数不能为空");
        
        // 创建分页参数
        PageReq pageRequest = PageHelper.createPageRequest(queryParam.getPageNum(), queryParam.getPageSize());
        
        try {
            // 调用userService获取用户列表
            List<UserDetailDTO> users = userService.listUsers(queryParam, pageRequest);
            // 获取总记录数
            long total = userService.countUsers(queryParam);
            // 转换为VO对象
            List<UserListItemVO> result = users.stream()
                    .map(userStructMapper::toUserListItemVO)
                    .toList();
            
            // 构建分页结果
            return PageHelper.build(result, pageRequest.getPageNum(), pageRequest.getPageSize(), total);
            
        } catch (Exception e) {
            ExceptionUtil.error(StatusEnum.SYSTEM_ERROR, "查询用户列表失败", e);
            return null; // 不会执行到这里，因为ExceptionUtil.error会抛出异常
        }
    }

    /**
     * 查询用户详细信息
     *
     * @param userId 用户ID
     * @return 用户详细信息
     */
    @Override
    public UserDetailDTO getUserDetail(Long userId) {
        ExceptionUtil.requireNonNull(userId, StatusEnum.PARAM_MISSING, "用户ID为空");
        return userService.getUserDetail(userId);
    }

    /**
     * 修改用户状态
     *
     * @param userId 用户ID
     * @param status 新状态
     */
    @Override
    public void updateStatus(Long userId, UserStatusEnum status) {
        ExceptionUtil.requireNonNull(userId, StatusEnum.PARAM_MISSING, "用户ID");
        ExceptionUtil.requireNonNull(status, StatusEnum.PARAM_MISSING, "用户状态");

        userService.updateStatus(userId, status);
    }
}
