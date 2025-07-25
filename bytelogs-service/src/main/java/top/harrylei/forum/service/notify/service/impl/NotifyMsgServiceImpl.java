package top.harrylei.forum.service.notify.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.harrylei.forum.api.model.enums.NotifyMsgStateEnum;
import top.harrylei.forum.api.model.enums.NotifyTypeEnum;
import top.harrylei.forum.api.model.event.NotificationEvent;
import top.harrylei.forum.api.model.vo.notify.dto.NotifyMsgDTO;
import top.harrylei.forum.api.model.vo.notify.req.NotifyMsgQueryParam;
import top.harrylei.forum.api.model.vo.notify.vo.NotifyMsgVO;
import top.harrylei.forum.core.util.PageHelper;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.api.model.vo.user.dto.UserInfoDetailDTO;
import top.harrylei.forum.service.notify.converted.NotifyMsgStructMapper;
import top.harrylei.forum.service.notify.repository.dao.NotifyMsgDAO;
import top.harrylei.forum.service.notify.repository.entity.NotifyMsgDO;
import top.harrylei.forum.service.notify.service.NotifyMsgService;
import top.harrylei.forum.service.user.service.cache.UserCacheService;

/**
 * 通知消息服务实现
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyMsgServiceImpl implements NotifyMsgService {

    private final NotifyMsgDAO notifyMsgDAO;
    private final UserCacheService userCacheService;
    private final NotifyMsgStructMapper notifyMsgStructMapper;

    @Override
    public void saveNotificationFromEvent(NotificationEvent event) {
        try {
            // 1. 业务过滤：检查是否需要保存通知
            if (!shouldSaveNotification(event)) {
                log.debug("跳过通知事件: eventId={}, 原因=业务过滤", event.getEventId());
                return;
            }

            // 2. 构建通知消息
            NotifyMsgDO notifyMsg = buildNotifyMessage(event);

            // 3. 保存到数据库
            notifyMsgDAO.save(notifyMsg);


        } catch (Exception e) {
            log.error("处理通知事件失败: eventId={}", event.getEventId(), e);
            throw e;
        }
    }

    /**
     * 判断是否需要保存通知到数据库
     *
     * @param event 通知事件
     * @return true-需要保存，false-跳过
     */
    private boolean shouldSaveNotification(NotificationEvent event) {
        // 1. 自己的操作不需要通知自己
        if (event.getOperateUserId().equals(event.getTargetUserId())) {
            return false;
        }

        // 2. 系统通知总是发送
        NotifyTypeEnum notifyType = NotifyTypeEnum.fromCode(event.getNotifyType());
        if (notifyType != null && notifyType.isSystemNotification()) {
            return true;
        }

        // 3. 可以在这里添加用户通知偏好设置检查
        // TODO 例如：用户是否关闭了某类型通知

        return true;
    }

    /**
     * 构建通知消息
     *
     * @param event 通知事件
     * @return 通知消息DO
     */
    private NotifyMsgDO buildNotifyMessage(NotificationEvent event) {
        // 获取操作用户信息
        UserInfoDetailDTO operateUser = userCacheService.getUserInfo(event.getOperateUserId());
        String operateUserName = operateUser.getUserName();

        // 构建通知消息内容
        String message = buildMessageContent(event, operateUserName);

        return new NotifyMsgDO()
                .setRelatedId(event.getRelatedId())
                .setNotifyUserId(event.getTargetUserId())
                .setOperateUserId(event.getOperateUserId())
                .setMsg(message)
                .setType(event.getNotifyType())
                .setContentType(event.getContentType())
                .setState(NotifyMsgStateEnum.UNREAD.getCode());
    }

    /**
     * 构建通知消息内容
     *
     * @param event           通知事件
     * @param operateUserName 操作用户名
     * @return 消息内容
     */
    private String buildMessageContent(NotificationEvent event, String operateUserName) {
        NotifyTypeEnum notifyType = NotifyTypeEnum.fromCode(event.getNotifyType());

        return switch (notifyType) {
            case PRAISE -> String.format("%s 赞了你的文章", operateUserName);
            case COMMENT -> String.format("%s 评论了你的文章", operateUserName);
            case REPLY -> String.format("%s 回复了你的评论", operateUserName);
            case COLLECT -> String.format("%s 收藏了你的文章", operateUserName);
            case FOLLOW -> String.format("%s 关注了你", operateUserName);
            case SYSTEM -> event.getExtra() != null ? event.getExtra() : "系统通知";
            case REGISTER -> String.format("欢迎 %s 加入 ByteLogs", operateUserName);
            case LOGIN -> String.format("%s 登录了系统", operateUserName);
            case ARTICLE_PUBLISH -> String.format("%s 发布了新文章", operateUserName);
        };
    }

    @Override
    public PageVO<NotifyMsgVO> getMyNotifications(Long userId, NotifyMsgQueryParam param) {
        // TODO: 根据查询参数构建查询条件（状态、类型过滤）
        Page<NotifyMsgDO> page = notifyMsgDAO.pageByUserId(userId, param.getPageNum(), param.getPageSize());
        return PageHelper.buildAndMap(page, this::fillNotifyMsgVO);
    }

    @Override
    public void markAsRead(Long msgId, Long userId) {
        boolean success = notifyMsgDAO.markAsRead(msgId, userId);
        if (success) {
            log.info("标记消息为已读: msgId={}, userId={}", msgId, userId);
        } else {
            log.warn("标记消息为已读失败: msgId={}, userId={}", msgId, userId);
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        boolean success = notifyMsgDAO.markAllAsRead(userId);
        if (success) {
            log.info("标记全部消息为已读: userId={}", userId);
        } else {
            log.warn("标记全部消息为已读失败: userId={}", userId);
        }
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return notifyMsgDAO.countUnreadByUserId(userId);
    }

    /**
     * 填充通知消息VO信息
     *
     * @param notifyMsg 通知消息DO
     * @return 通知消息VO
     */
    private NotifyMsgVO fillNotifyMsgVO(NotifyMsgDO notifyMsg) {
        NotifyMsgDTO dto = notifyMsgStructMapper.toDTO(notifyMsg);

        // 填充操作用户信息
        if (notifyMsg.getOperateUserId() != null) {
            UserInfoDetailDTO operateUser = userCacheService.getUserInfo(notifyMsg.getOperateUserId());
            if (operateUser != null) {
                dto.setOperateUserName(operateUser.getUserName());
                dto.setOperateUserAvatar(operateUser.getAvatar());
            }
        }

        // 转换为VO
        return notifyMsgStructMapper.toVO(dto);
    }
}