package top.harrylei.forum.service.user.repository.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.enums.user.UserFollowStatusEnum;
import top.harrylei.forum.api.model.vo.user.req.UserFollowQueryParam;
import top.harrylei.forum.api.model.vo.user.vo.UserFollowVO;
import top.harrylei.forum.service.user.repository.entity.UserFollowDO;
import top.harrylei.forum.service.user.repository.mapper.UserFollowMapper;

import java.util.List;

/**
 * 用户关注数据访问对象
 *
 * @author harry
 */
@Repository
public class UserFollowDAO extends ServiceImpl<UserFollowMapper, UserFollowDO> {

    /**
     * 根据关注者和要关注的用户ID查询关注关系
     *
     * @param userId       关注者ID
     * @param followUserId 要关注的用户ID
     * @return 关注关系，不存在则返回null
     */
    public UserFollowDO getFollowRelation(Long userId, Long followUserId) {
        if (userId == null || followUserId == null) {
            return null;
        }

        return lambdaQuery()
                .eq(UserFollowDO::getUserId, userId)
                .eq(UserFollowDO::getFollowUserId, followUserId)
                .eq(UserFollowDO::getDeleted, YesOrNoEnum.NO.getCode())
                .one();
    }

    /**
     * 查询用户的关注列表
     *
     * @param queryParam 查询参数
     * @param page       分页参数
     * @return 关注列表
     */
    public IPage<UserFollowVO> pageFollowingList(UserFollowQueryParam queryParam, IPage<UserFollowVO> page) {
        return getBaseMapper().pageFollowingList(queryParam, page);
    }

    /**
     * 查询用户的粉丝列表
     *
     * @param queryParam 查询参数
     * @param page       分页参数
     * @return 粉丝列表
     */
    public IPage<UserFollowVO> pageFollowersList(UserFollowQueryParam queryParam, IPage<UserFollowVO> page) {
        return getBaseMapper().pageFollowersList(queryParam, page);
    }

    /**
     * 更新关注状态
     *
     * @param userId       关注者ID
     * @param followUserId 被关注者ID
     * @param status       关注状态
     * @return 更新结果
     */
    public boolean updateFollowStatus(Long userId, Long followUserId, UserFollowStatusEnum status) {
        if (userId == null || followUserId == null || status == null) {
            return false;
        }

        return lambdaUpdate()
                .eq(UserFollowDO::getUserId, userId)
                .eq(UserFollowDO::getFollowUserId, followUserId)
                .eq(UserFollowDO::getDeleted, YesOrNoEnum.NO.getCode())
                .set(UserFollowDO::getFollowState, status.getCode())
                .update();
    }

    /**
     * 获取用户的关注者ID列表
     *
     * @param userId 用户ID
     * @return 关注者ID列表
     */
    public List<Long> getFollowerIds(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return getBaseMapper().getFollowerIds(userId);
    }
}