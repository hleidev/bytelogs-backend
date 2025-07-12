package top.harrylei.forum.service.notify.service;

import top.harrylei.forum.api.model.event.NotificationEvent;

/**
 * 通知服务接口
 *
 * @author harry
 */
public interface NotificationService {

    /**
     * 处理通知事件
     *
     * @param event 通知事件
     */
    void processNotificationEvent(NotificationEvent event);
}