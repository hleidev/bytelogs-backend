package top.harrylei.forum.service.user.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.harrylei.forum.api.model.vo.user.dto.UserDetailDTO;
import top.harrylei.forum.service.user.repository.entity.UserDO;

import java.time.LocalDateTime;
import java.util.List;

public interface UserMapper extends BaseMapper<UserDO> {

    /**
     * 联表查询用户完整信息
     */
    List<UserDetailDTO> listUsers(String userName, Integer status, Integer deleted, LocalDateTime startTime,
        LocalDateTime endTime, String orderBySql, String limitSql);

    /**
     * 统计符合条件的用户数量
     */
    long countUsers(String userName, Integer status, Integer deleted, LocalDateTime startTime, LocalDateTime endTime);
}
