package top.harrylei.forum.service.user.repository.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.vo.page.param.UserQueryParam;
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

    public List<UserDO> listUsers(UserQueryParam queryParam, String limitSql) {
        return lambdaQuery()
                .eq(queryParam.getStatus() != null, UserDO::getStatus, queryParam.getStatus())
                .eq(queryParam.getDeleted() != null, UserDO::getDeleted, queryParam.getDeleted())
                .ge(queryParam.getStartTime() != null, UserDO::getCreateTime, queryParam.getStartTime())
                .le(queryParam.getEndTime() != null, UserDO::getCreateTime, queryParam.getEndTime())
                .like(queryParam.getUserName() != null && !queryParam.getUserName().isEmpty(), UserDO::getUserName, queryParam.getUserName())
                .orderByDesc(UserDO::getCreateTime)
                .last(limitSql)
                .list();
    }
}