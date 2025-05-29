package top.harrylei.forum.service.user.repository.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.service.user.repository.entity.UserDO;
import top.harrylei.forum.service.user.repository.entity.UserInfoDO;
import top.harrylei.forum.service.user.repository.mapper.UserInfoMapper;
import top.harrylei.forum.service.user.repository.mapper.UserMapper;

@Repository
public class UserDAO extends ServiceImpl<UserInfoMapper, UserInfoDO> {

    @Autowired
    private UserMapper userMapper;

    public UserDO getUserByUserName(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = new LambdaQueryWrapper<UserDO>()
                .eq(UserDO::getUserName, username)
                .eq(UserDO::getDeleted, YesOrNoEnum.NO.getCode())
                .last("limit 1");
        return userMapper.selectOne(queryWrapper);
    }

    public void saveUser(UserDO user) {
        if (user != null) {
            userMapper.insert(user);
        } else {
            userMapper.updateById(user);
        }
    }
}
