package top.harrylei.forum.service.admin.service;

import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.api.model.vo.page.param.UserQueryParam;
import top.harrylei.forum.api.model.vo.user.vo.UserListItemVO;

public interface UserManagementService {

    /**
     * 查询用户列表
     *
     * @param queryParam 查询参数
     * @return 用户列表的分页结果
     */
    PageVO<UserListItemVO> list(UserQueryParam queryParam);
}
