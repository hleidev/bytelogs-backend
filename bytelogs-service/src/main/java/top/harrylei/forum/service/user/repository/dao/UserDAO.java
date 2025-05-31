package top.harrylei.forum.service.user.repository.dao;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.service.user.repository.entity.UserDO;
import top.harrylei.forum.service.user.repository.mapper.UserMapper;

/**
 * 用户账号数据访问对象
 * 负责操作user_account表
 */
@Repository
public class UserDAO extends ServiceImpl<UserMapper, UserDO> {

    /**
     * 根据用户名查询用户账号信息
     *
     * @param username 用户名
     * @return 用户账号信息，不存在则返回null
     */
    public UserDO getUserByUserName(String username) {
        if (username == null) {
            return null;
        }

        return lambdaQuery()
                .eq(UserDO::getUserName, username)
                .eq(UserDO::getDeleted, YesOrNoEnum.NO.getCode())
                .last("limit 1")
                .one();
    }

    /**
     * 保存或更新用户账号信息
     *
     * @param user 用户账号信息
     */
    public void saveUser(UserDO user) {
        if (user.getId() == null) {
            baseMapper.insert(user);
        } else {
            baseMapper.updateById(user);
        }
    }
} 