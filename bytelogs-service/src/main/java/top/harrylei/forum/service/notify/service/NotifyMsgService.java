package top.harrylei.forum.service.notify.service;

import top.harrylei.forum.api.model.event.NotificationEvent;
import top.harrylei.forum.api.model.vo.notify.dto.NotifyMsgDTO;
import top.harrylei.forum.api.model.vo.notify.req.NotifyMsgQueryParam;
import top.harrylei.forum.api.model.vo.page.PageVO;

/**
 * 通知消息服务接口
 *
 * @author harry
 */
public interface NotifyMsgService {

    /**
     * 从事件保存通知消息
     *
     * @param event 通知事件
     */
    void saveNotificationFromEvent(NotificationEvent event);

    /**
     * 分页查询我的通知消息
     *
     * @param userId 用户ID
     * @param param  查询参数
     * @return 通知消息分页结果
     */
    PageVO<NotifyMsgDTO> getMyNotifications(Long userId, NotifyMsgQueryParam param);

    /**
     * 标记消息为已读
     *
     * @param msgId 消息ID
     */
    void markAsRead(Long msgId);

    /**
     * 标记用户所有消息为已读
     *
     * @param userId 用户ID
     */
    void markAllAsRead(Long userId);

    /**
     * 获取用户未读消息数量
     *
     * @param userId 用户ID
     * @return 未读消息数量
     */
    Long getUnreadCount(Long userId);
}