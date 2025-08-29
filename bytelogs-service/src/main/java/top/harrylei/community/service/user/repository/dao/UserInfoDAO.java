package top.harrylei.community.service.user.repository.dao;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.service.user.repository.entity.UserInfoDO;
import top.harrylei.community.service.user.repository.mapper.UserInfoMapper;

import java.util.List;

/**
 * 用户信息数据访问对象
 *
 * @author harry
 */
@Repository
public class UserInfoDAO extends ServiceImpl<UserInfoMapper, UserInfoDO> {


    public UserInfoDO getByUserId(Long userId) {
        return lambdaQuery()
                .eq(UserInfoDO::getUserId, userId)
                .eq(UserInfoDO::getDeleted, DeleteStatusEnum.NOT_DELETED)
                .one();
    }

    public List<UserInfoDO> listByUserIds(List<Long> userIds) {
        return lambdaQuery()
                .in(UserInfoDO::getUserId, userIds)
                .eq(UserInfoDO::getDeleted, DeleteStatusEnum.NOT_DELETED)
                .list();
    }
}