package top.harrylei.forum.service.admin.service;

import top.harrylei.forum.api.model.enums.user.UserStatusEnum;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.api.model.vo.page.param.UserQueryParam;
import top.harrylei.forum.api.model.vo.user.dto.UserDetailDTO;
import top.harrylei.forum.api.model.vo.user.vo.UserListItemVO;

public interface UserManagementService {

    /**
     * 查询用户列表
     *
     * @param queryParam 查询参数
     * @return 用户列表的分页结果
     */
    PageVO<UserListItemVO> list(UserQueryParam queryParam);

    /**
     * 查询用户详细信息
     *
     * @param userId 用户ID
     * @return 用户详细信息
     */
    UserDetailDTO getUserDetail(Long userId);

    /**
     * 修改用户状态
     *
     * @param userId 用户ID
     * @param status 新状态
     */
    void updateStatus(Long userId, UserStatusEnum status);
}
