package top.harrylei.community.service.user.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import top.harrylei.community.api.model.page.param.UserQueryParam;
import top.harrylei.community.api.model.user.dto.UserDetailDTO;
import top.harrylei.community.service.user.repository.entity.UserDO;

public interface UserMapper extends BaseMapper<UserDO> {

    /**
     * 联表查询用户完整信息（支持MyBatis-Plus分页）
     */
    IPage<UserDetailDTO> pageUsers(IPage<UserDetailDTO> page, UserQueryParam queryParam);

    /**
     * 查询用户详细信息
     */
    UserDetailDTO selectUserDetail(Long userId);
}
