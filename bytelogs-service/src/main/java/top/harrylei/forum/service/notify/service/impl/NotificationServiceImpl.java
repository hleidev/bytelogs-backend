package top.harrylei.forum.service.notify.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.harrylei.forum.api.model.event.NotificationEvent;
import top.harrylei.forum.api.model.vo.user.dto.UserInfoDetailDTO;
import top.harrylei.forum.service.notify.repository.dao.NotifyMsgDAO;
import top.harrylei.forum.service.notify.repository.entity.NotifyMsgDO;
import top.harrylei.forum.service.notify.service.NotificationService;
import top.harrylei.forum.service.user.service.cache.UserCacheService;

/**
 * 通知服务实现
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotifyMsgDAO notifyMsgDAO;
    private final UserCacheService userCacheService;

    @Override
    public void processNotificationEvent(NotificationEvent event) {
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

            log.info("通知消息保存成功: eventId={}, msgId={}", event.getEventId(), notifyMsg.getId());

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
        if (event.getNotifyType().getCode() >= 6) {
            return true;
        }

        // 3. 检查目标用户是否存在（防御性编程，理论上不应该发生）
        UserInfoDetailDTO targetUser = userCacheService.getUserInfo(event.getTargetUserId());
        if (targetUser == null) {
            log.error("目标用户不存在，数据异常: userId={}, eventId={}", event.getTargetUserId(), event.getEventId());
            return false;
        }

        // 4. 可以在这里添加用户通知偏好设置检查
        // 例如：用户是否关闭了某类型通知

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
                .setType(event.getNotifyType().getCode())
                .setState(0);
    }

    /**
     * 构建通知消息内容
     *
     * @param event           通知事件
     * @param operateUserName 操作用户名
     * @return 消息内容
     */
    private String buildMessageContent(NotificationEvent event, String operateUserName) {
        return switch (event.getNotifyType()) {
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
}