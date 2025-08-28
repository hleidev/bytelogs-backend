package top.harrylei.community.service.user.repository.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.community.api.enums.YesOrNoEnum;
import top.harrylei.community.api.model.page.param.UserQueryParam;
import top.harrylei.community.api.model.user.dto.UserDetailDTO;
import top.harrylei.community.service.user.repository.entity.UserDO;
import top.harrylei.community.service.user.repository.mapper.UserMapper;

/**
 * 用户账号数据访问对象 负责操作user_account表
 *
 * @author harry
 */
@Repository
public class UserDAO extends ServiceImpl<UserMapper, UserDO> {

    /**
     * 根据用户名查询用户账号信息
     *
     * @param username 用户名
     * @return 用户账号信息，不存在则返回null
     */
    public UserDO getUserByUsername(String username) {
        if (username == null) {
            return null;
        }

        return lambdaQuery()
                .eq(UserDO::getUserName, username)
                .eq(UserDO::getDeleted, YesOrNoEnum.NO.getCode())
                .one();
    }

    public IPage<UserDetailDTO> pageUsers(UserQueryParam queryParam, IPage<UserDetailDTO> page) {
        return getBaseMapper().pageUsers(page, queryParam);
    }

    /**
     * 查询用户详细信息
     *
     * @param userId 用户ID
     * @return 用户详细信息
     */
    public UserDetailDTO getUserDetail(Long userId) {
        return getBaseMapper()
                .selectUserDetail(userId);
    }

    /**
     * 通过userId获取用户账号信息
     *
     * @param userId 用户ID
     * @return 用户账号信息
     */
    public UserDO getUserById(Long userId) {
        return lambdaQuery()
                .eq(UserDO::getId, userId)
                .eq(UserDO::getDeleted, YesOrNoEnum.NO.getCode())
                .one();
    }
}