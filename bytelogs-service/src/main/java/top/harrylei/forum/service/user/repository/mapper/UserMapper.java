package top.harrylei.forum.service.user.repository.mapper;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import top.harrylei.forum.api.model.vo.page.param.UserQueryParam;
import top.harrylei.forum.api.model.vo.user.dto.UserDetailDTO;
import top.harrylei.forum.service.user.repository.entity.UserDO;

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
