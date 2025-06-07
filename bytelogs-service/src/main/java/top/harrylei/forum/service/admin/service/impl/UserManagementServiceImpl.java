package top.harrylei.forum.service.admin.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.StatusEnum;
import top.harrylei.forum.api.model.vo.page.PageHelper;
import top.harrylei.forum.api.model.vo.page.PageReq;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.api.model.vo.page.param.UserQueryParam;
import top.harrylei.forum.api.model.vo.user.dto.UserDetailDTO;
import top.harrylei.forum.api.model.vo.user.vo.UserListItemVO;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.service.admin.service.UserManagementService;
import top.harrylei.forum.service.user.converted.UserStructMapper;
import top.harrylei.forum.service.user.repository.dao.UserDAO;
import top.harrylei.forum.service.user.repository.dao.UserInfoDAO;
import top.harrylei.forum.service.user.service.UserService;

/**
 * 用户管理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserDAO userDAO;
    private final UserService userService;
    private final UserInfoDAO userInfoDAO;
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
            log.error("查询用户列表异常: queryParam={}", queryParam, e);
            ExceptionUtil.error(StatusEnum.SYSTEM_ERROR, "查询用户列表失败");
            return null; // 不会执行到这里，因为ExceptionUtil.error会抛出异常
        }
    }
}
