package top.harrylei.forum.service.user.repository.dao;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import top.harrylei.forum.api.enums.YesOrNoEnum;
import top.harrylei.forum.service.user.repository.entity.UserInfoDO;
import top.harrylei.forum.service.user.repository.mapper.UserInfoMapper;

import java.util.List;

/**
 * 用户信息数据访问对象
 * 负责操作user_info表
 */
@Repository
public class UserInfoDAO extends ServiceImpl<UserInfoMapper, UserInfoDO> {


    public UserInfoDO getByUserId(Long userId) {
        return lambdaQuery()
                .eq(UserInfoDO::getUserId, userId)
                .eq(UserInfoDO::getDeleted, YesOrNoEnum.NO.getCode())
                .one();
    }

    public UserInfoDO getDeletedByUserId(Long userId) {
        return lambdaQuery()
                .eq(UserInfoDO::getUserId, userId)
                .eq(UserInfoDO::getDeleted, YesOrNoEnum.YES.getCode())
                .one();
    }

    public UserInfoDO getByUserName(String username) {
        return lambdaQuery()
                .eq(UserInfoDO::getUserName, username)
                .eq(UserInfoDO::getDeleted, YesOrNoEnum.NO.getCode())
                .one();
    }

    public List<UserInfoDO> queryBatchByUserIds(List<Long> userIds) {
        return lambdaQuery()
                .in(UserInfoDO::getUserId, userIds)
                .eq(UserInfoDO::getDeleted, YesOrNoEnum.NO.getCode())
                .list();
    }
}