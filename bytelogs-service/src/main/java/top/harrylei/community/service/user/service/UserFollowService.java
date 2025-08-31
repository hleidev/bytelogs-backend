package top.harrylei.community.service.user.service;

import top.harrylei.community.api.model.page.PageVO;
import top.harrylei.community.api.model.user.req.UserFollowQueryParam;
import top.harrylei.community.api.model.user.vo.UserFollowVO;

import java.util.List;

/**
 * 用户关注服务接口
 *
 * @author harry
 */
public interface UserFollowService {

    /**
     * 关注用户
     *
     * @param followUserId 要关注的用户ID
     */
    void followUser(Long followUserId);

    /**
     * 取消关注用户
     *
     * @param followUserId 要取消关注的用户ID
     */
    void unfollowUser(Long followUserId);

    /**
     * 分页查询用户关注列表
     *
     * @param queryParam 查询参数
     * @return 关注列表
     */
    PageVO<UserFollowVO> pageFollowingList(UserFollowQueryParam queryParam);

    /**
     * 分页查询用户粉丝列表
     *
     * @param queryParam 查询参数
     * @return 粉丝列表
     */
    PageVO<UserFollowVO> pageFollowersList(UserFollowQueryParam queryParam);

    /**
     * 获取用户的关注者ID列表
     *
     * @param userId 用户ID
     * @return 关注者ID列表
     */
    List<Long> listFollowerIds(Long userId);
}