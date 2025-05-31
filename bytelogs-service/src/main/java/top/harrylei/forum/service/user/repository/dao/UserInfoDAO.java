package top.harrylei.forum.service.user.repository.dao;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import top.harrylei.forum.service.user.repository.entity.UserInfoDO;
import top.harrylei.forum.service.user.repository.mapper.UserInfoMapper;

/**
 * 用户信息数据访问对象
 * 负责操作user_info表
 */
@Repository
public class UserInfoDAO extends ServiceImpl<UserInfoMapper, UserInfoDO> {

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息，不存在则返回null
     */
    public UserInfoDO getByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        
        return lambdaQuery()
                .eq(UserInfoDO::getUserId, userId)
                .last("limit 1")
                .one();
    }
} 