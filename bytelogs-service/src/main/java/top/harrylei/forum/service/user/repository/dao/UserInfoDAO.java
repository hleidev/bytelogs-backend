package top.harrylei.forum.service.user.repository.dao;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.service.user.repository.entity.UserInfoDO;
import top.harrylei.forum.service.user.repository.mapper.UserInfoMapper;

/**
 * 用户信息数据访问对象
 * 负责操作user_info表
 */
@Repository
public class UserInfoDAO extends ServiceImpl<UserInfoMapper, UserInfoDO> {


    public UserInfoDO getUserInfoById(Long userId) {
        return lambdaQuery()
                .eq(UserInfoDO::getUserId, userId)
                .eq(UserInfoDO::getDeleted, YesOrNoEnum.NO.getCode())
                .one();
    }

    public UserInfoDO getDeletedUserInfoById(Long userId) {
        return lambdaQuery()
                .eq(UserInfoDO::getUserId, userId)
                .eq(UserInfoDO::getDeleted, YesOrNoEnum.YES.getCode())
                .one();
    }
}