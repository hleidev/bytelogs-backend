package top.harrylei.community.service.user.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.harrylei.community.api.enums.response.ResultCode;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.api.enums.article.ContentTypeEnum;
import top.harrylei.community.api.enums.notify.NotifyTypeEnum;
import top.harrylei.community.api.enums.rank.ActivityActionEnum;
import top.harrylei.community.api.enums.rank.ActivityTargetEnum;
import top.harrylei.community.api.enums.user.UserFollowStatusEnum;
import top.harrylei.community.api.model.page.PageVO;
import top.harrylei.community.api.model.user.req.UserFollowQueryParam;
import top.harrylei.community.api.model.user.vo.UserFollowVO;
import top.harrylei.community.core.context.ReqInfoContext;
import top.harrylei.community.core.util.KafkaEventPublisher;
import top.harrylei.community.core.util.PageUtils;
import top.harrylei.community.core.util.RedisUtil;
import top.harrylei.community.service.user.repository.dao.UserDAO;
import top.harrylei.community.service.user.repository.dao.UserFollowDAO;
import top.harrylei.community.service.user.repository.entity.UserDO;
import top.harrylei.community.service.user.repository.entity.UserFollowDO;
import top.harrylei.community.service.user.service.UserFollowService;

import java.time.Duration;
import java.util.List;
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
    private final RedisUtil redisUtil;
    private final KafkaEventPublisher kafkaEventPublisher;

    /**
     * 防重复提交锁过期时间（秒）
     */
    private static final Duration DUPLICATE_PREVENT_TIME = Duration.ofSeconds(2);

    /**
     * 关注用户
     *
     * @param followUserId 要关注的用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void followUser(Long followUserId) {
        Long currentUserId = ReqInfoContext.getContext().getUserId();
        validateFollowParams(currentUserId, followUserId);
        // 检查用户是否存在
        validateUsersExist(currentUserId, followUserId);

        // 不能关注自己
        if (Objects.equals(currentUserId, followUserId)) {
            ResultCode.INVALID_PARAMETER.throwException("不能关注自己");
        }

        // 防重复提交检查
        String duplicateKey = buildFollowDuplicateKey(currentUserId, followUserId, "FOLLOW");
        if (!redisUtil.tryPreventDuplicate(duplicateKey, DUPLICATE_PREVENT_TIME)) {
            log.warn("检测到重复提交: userId={} followUserId={} operation=FOLLOW", currentUserId, followUserId);
            return;
        }

        // 查询是否已有关注关系
        UserFollowDO existingFollow = userFollowDAO.getFollowRelation(currentUserId, followUserId);

        boolean success = false;

        if (existingFollow == null) {
            // 创建新的关注关系
            UserFollowDO newFollow = new UserFollowDO()
                    .setUserId(currentUserId)
                    .setFollowUserId(followUserId)
                    .setFollowState(UserFollowStatusEnum.FOLLOWED)
                    .setDeleted(DeleteStatusEnum.NOT_DELETED);
            userFollowDAO.save(newFollow);
            success = true;
        } else if (!UserFollowStatusEnum.FOLLOWED.equals(existingFollow.getFollowState())) {
            // 更新现有关注关系状态
            userFollowDAO.updateFollowStatus(currentUserId, followUserId, UserFollowStatusEnum.FOLLOWED);
            success = true;
        } else {
            // 已经关注，无需重复操作
            log.warn("用户已关注，无需重复关注 userId={} followeeId={}", currentUserId, followUserId);
        }

        if (success) {
            // 发布关注通知事件
            publishFollowNotificationEvent(currentUserId, followUserId);
            // 发布关注活跃度事件
            publishFollowActivityEvent(currentUserId, followUserId, ActivityActionEnum.FOLLOW);
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

        // 统一使用校验方法
        validateFollowParams(currentUserId, followUserId);

        // 检查是否已关注
        UserFollowDO relation = userFollowDAO.getFollowRelation(currentUserId, followUserId);

        // 如果没有关注关系或状态不是已关注，则直接返回
        if (relation == null || !UserFollowStatusEnum.FOLLOWED.equals(relation.getFollowState())) {
            return;
        }

        // 防重复提交检查
        String duplicateKey = buildFollowDuplicateKey(currentUserId, followUserId, "UNFOLLOW");
        if (!redisUtil.tryPreventDuplicate(duplicateKey, DUPLICATE_PREVENT_TIME)) {
            log.warn("检测到重复提交: userId={} followUserId={} operation=UNFOLLOW", currentUserId, followUserId);
            return;
        }

        // 更新关注状态为未关注
        userFollowDAO.updateFollowStatus(currentUserId, followUserId, UserFollowStatusEnum.UNFOLLOWED);

        // 发布取消关注活跃度事件
        publishFollowActivityEvent(currentUserId, followUserId, ActivityActionEnum.CANCEL_FOLLOW);

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
        IPage<UserFollowVO> page = PageUtils.of(queryParam);

        // 查询关注列表
        IPage<UserFollowVO> result = userFollowDAO.pageFollowingList(queryParam, page);

        // 使用PageHelper构建分页结果
        return PageUtils.from(result);
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
        IPage<UserFollowVO> page = PageUtils.of(queryParam);

        // 查询粉丝列表
        IPage<UserFollowVO> result = userFollowDAO.pageFollowersList(queryParam, page);

        // 使用PageHelper构建分页结果
        return PageUtils.from(result);
    }

    /**
     * 校验关注参数
     *
     * @param userId       关注者ID
     * @param followUserId 被关注者ID
     */
    private void validateFollowParams(Long userId, Long followUserId) {
        if (userId == null || followUserId == null) {
            ResultCode.INVALID_PARAMETER.throwException("用户ID或被关注用户ID不能为空");
        }
    }

    private void validateUsersExist(Long userId, Long followUserId) {
        // 检查关注者是否存在
        UserDO user = userDAO.getUserById(userId);
        if (user == null) {
            ResultCode.USER_NOT_EXISTS.throwException("关注者不存在");
        }

        // 检查被关注者是否存在
        UserDO followUser = userDAO.getUserById(followUserId);
        if (followUser == null) {
            ResultCode.USER_NOT_EXISTS.throwException("被关注用户不存在");
        }
    }

    /**
     * 构建关注操作的防重复提交Key
     *
     * @param userId       用户ID
     * @param followUserId 被关注用户ID
     * @param operation    操作类型（FOLLOW/UNFOLLOW）
     * @return 防重复提交Key
     */
    private String buildFollowDuplicateKey(Long userId, Long followUserId, String operation) {
        return String.format("%d:%s:%d", userId, operation, followUserId);
    }

    /**
     * 发布关注通知事件
     *
     * @param currentUserId 当前用户ID
     * @param followUserId  被关注用户ID
     */
    private void publishFollowNotificationEvent(Long currentUserId, Long followUserId) {
        try {
            kafkaEventPublisher.publishUserBehaviorEvent(currentUserId,
                                                         followUserId,
                                                         followUserId,
                                                         ContentTypeEnum.EMPTY,
                                                         NotifyTypeEnum.FOLLOW);

            log.debug("发布关注通知事件成功: currentUserId={}, followUserId={}", currentUserId, followUserId);

        } catch (Exception e) {
            log.error("发布关注通知事件失败: currentUserId={}, followUserId={}", currentUserId, followUserId, e);
        }
    }

    /**
     * 发布关注活跃度事件
     *
     * @param currentUserId  当前用户ID
     * @param followUserId   被关注用户ID
     * @param activityAction 活跃度行为（关注/取消关注）
     */
    private void publishFollowActivityEvent(Long currentUserId, Long followUserId, ActivityActionEnum activityAction) {
        try {
            kafkaEventPublisher.publishUserActivityEvent(currentUserId,
                                                         followUserId,
                                                         ActivityTargetEnum.USER,
                                                         activityAction);

            log.debug("发布{}活跃度事件成功: currentUserId={}, followUserId={}",
                      activityAction.getLabel(), currentUserId, followUserId);

        } catch (Exception e) {
            log.error("发布{}活跃度事件失败: currentUserId={}, followUserId={}",
                      activityAction.getLabel(), currentUserId, followUserId, e);
        }
    }

    @Override
    public List<Long> getFollowerIds(Long userId) {
        return userFollowDAO.getFollowerIds(userId);
    }
}