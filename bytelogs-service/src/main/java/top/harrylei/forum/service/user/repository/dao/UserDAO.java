package top.harrylei.forum.service.user.repository.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.vo.page.param.UserQueryParam;
import top.harrylei.forum.api.model.vo.user.dto.UserDetailDTO;
import top.harrylei.forum.service.user.repository.entity.UserDO;
import top.harrylei.forum.service.user.repository.mapper.UserMapper;

/**
 * 用户账号数据访问对象 负责操作user_account表
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
     * 根据查询参数和分页信息查询用户列表
     *
     * @param queryParam 查询参数
     * @param limitSql 分页SQL
     * @return 用户列表
     */
    public List<UserDetailDTO> listUsers(UserQueryParam queryParam, String limitSql) {
        String orderBySql = queryParam.getOrderBySql();

        return getBaseMapper()
                .listUsers(
                        queryParam.getUserName(),
                        queryParam.getStatus(),
                        queryParam.getDeleted(),
                        queryParam.getStartTime(),
                        queryParam.getEndTime(),
                        orderBySql, limitSql
        );
    }

    /**
     * 统计符合条件的用户数量
     *
     * @param queryParam 查询参数
     * @return 用户数量
     */
    public long countUsers(UserQueryParam queryParam) {
        return getBaseMapper()
                .countUsers(
                        queryParam.getUserName(),
                        queryParam.getStatus(),
                        queryParam.getDeleted(),
                        queryParam.getStartTime(),
                        queryParam.getEndTime()
                );
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

    /**
     * 通过userId查询被删除的账号信息
     *
     * @param userId 用户ID
     * @return 用户账号信息
     */
    public UserDO getDeletedUserById(Long userId) {
        return lambdaQuery()
                .eq(UserDO::getId, userId)
                .eq(UserDO::getDeleted, YesOrNoEnum.YES.getCode())
                .one();
    }
}