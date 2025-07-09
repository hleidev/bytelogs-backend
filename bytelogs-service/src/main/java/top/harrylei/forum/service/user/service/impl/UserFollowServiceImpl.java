package top.harrylei.forum.service.user.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.harrylei.forum.api.model.enums.ErrorCodeEnum;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.enums.user.UserFollowStatusEnum;
import top.harrylei.forum.api.model.vo.page.PageHelper;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.api.model.vo.user.req.UserFollowQueryParam;
import top.harrylei.forum.api.model.vo.user.vo.UserFollowVO;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.service.user.repository.dao.UserDAO;
import top.harrylei.forum.service.user.repository.dao.UserFollowDAO;
import top.harrylei.forum.service.user.repository.entity.UserDO;
import top.harrylei.forum.service.user.repository.entity.UserFollowDO;
import top.harrylei.forum.service.user.service.UserFollowService;

import java.util.Objects;

/**
 * 用户关注服务实现类
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserFollowServiceImpl implements UserFollowService {

    private final UserFollowDAO userFollowDAO;
    private final UserDAO userDAO;

    /**
     * 关注用户
     *
     * @param followUserId 要关注的用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void followUser(Long followUserId) {
        Long currentUserId = ReqInfoContext.getContext().getUserId();

        // 参数校验
        validateFollowParams(currentUserId, followUserId);

        // 检查用户是否存在
        validateUsersExist(currentUserId, followUserId);

        // 不能关注自己
        if (Objects.equals(currentUserId, followUserId)) {
            ExceptionUtil.error(ErrorCodeEnum.PARAM_VALIDATE_FAILED, "不能关注自己");
        }

        // 查询是否已有关注关系
        UserFollowDO existingFollow = userFollowDAO.getFollowRelation(currentUserId, followUserId);

        if (existingFollow == null) {
            // 创建新的关注关系
            UserFollowDO newFollow = new UserFollowDO()
                    .setUserId(currentUserId)
                    .setFollowUserId(followUserId)
                    .setFollowState(UserFollowStatusEnum.FOLLOWED.getCode())
                    .setDeleted(YesOrNoEnum.NO.getCode());
            boolean saved = userFollowDAO.save(newFollow);
            ExceptionUtil.errorIf(!saved, ErrorCodeEnum.UNEXPECT_ERROR, "关注失败");
        } else if (!Objects.equals(existingFollow.getFollowState(), UserFollowStatusEnum.FOLLOWED.getCode())) {
            // 更新现有关注关系状态
            boolean updated = userFollowDAO.updateFollowStatus(currentUserId,
                                                               followUserId,
                                                               UserFollowStatusEnum.FOLLOWED);
            ExceptionUtil.errorIf(!updated, ErrorCodeEnum.UNEXPECT_ERROR, "关注失败");
        } else {
            // 已经关注，无需重复操作
            log.warn("用户已关注，无需重复关注 userId={} followeeId={}", currentUserId, followUserId);
        }

        log.info("用户关注成功 followerId={} followeeId={}", currentUserId, followUserId);
    }

    /**
     * 取消关注用户
     *
     * @param followUserId 要取消关注的用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollowUser(Long followUserId) {
        Long currentUserId = ReqInfoContext.getContext().getUserId();

        // 参数校验
        validateFollowParams(currentUserId, followUserId);

        // 检查是否已关注
        UserFollowDO relation = userFollowDAO.getFollowRelation(currentUserId, followUserId);
        ExceptionUtil.errorIf(!Objects.equals(relation.getFollowState(), UserFollowStatusEnum.FOLLOWED.getCode()),
                              ErrorCodeEnum.PARAM_VALIDATE_FAILED,
                              "未关注该用户，无法取消关注");

        // 更新关注状态为未关注
        boolean updated = userFollowDAO.updateFollowStatus(currentUserId,
                                                           followUserId,
                                                           UserFollowStatusEnum.UNFOLLOWED);
        ExceptionUtil.errorIf(!updated, ErrorCodeEnum.SYSTEM_ERROR, "取消关注失败");

        log.info("用户取消关注成功 followerId={} followeeId={}", currentUserId, followUserId);
    }


    /**
     * 分页查询用户关注列表
     *
     * @param queryParam 查询参数
     * @return 关注列表
     */
    @Override
    public PageVO<UserFollowVO> pageFollowingList(UserFollowQueryParam queryParam) {
        // 创建分页对象
        Page<UserFollowVO> page = new Page<>(queryParam.getPageNum(), queryParam.getPageSize());

        // 查询关注列表
        IPage<UserFollowVO> result = userFollowDAO.pageFollowingList(queryParam, page);

        // 使用PageHelper构建分页结果
        return PageHelper.build(result);
    }

    /**
     * 分页查询用户粉丝列表
     *
     * @param queryParam 查询参数
     * @return 粉丝列表
     */
    @Override
    public PageVO<UserFollowVO> pageFollowersList(UserFollowQueryParam queryParam) {
        // 创建分页对象
        Page<UserFollowVO> page = new Page<>(queryParam.getPageNum(), queryParam.getPageSize());

        // 查询粉丝列表
        IPage<UserFollowVO> result = userFollowDAO.pageFollowersList(queryParam, page);

        // 使用PageHelper构建分页结果
        return PageHelper.build(result);
    }

    /**
     * 校验关注参数
     *
     * @param userId       关注者ID
     * @param followUserId 被关注者ID
     */
    private void validateFollowParams(Long userId, Long followUserId) {
        ExceptionUtil.requireValid(userId, ErrorCodeEnum.PARAM_VALIDATE_FAILED, "关注者ID不能为空");
        ExceptionUtil.requireValid(followUserId, ErrorCodeEnum.PARAM_VALIDATE_FAILED, "被关注者ID不能为空");
    }

    private void validateUsersExist(Long userId, Long followUserId) {
        // 检查关注者是否存在
        UserDO user = userDAO.getUserById(userId);
        ExceptionUtil.requireValid(user, ErrorCodeEnum.USER_NOT_EXISTS, "关注者不存在");

        // 检查被关注者是否存在
        UserDO followUser = userDAO.getUserById(followUserId);
        ExceptionUtil.requireValid(followUser, ErrorCodeEnum.USER_NOT_EXISTS, "被关注者不存在");
    }
}