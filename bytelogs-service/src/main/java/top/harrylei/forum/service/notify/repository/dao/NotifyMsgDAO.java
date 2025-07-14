package top.harrylei.forum.service.notify.repository.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.forum.service.notify.repository.entity.NotifyMsgDO;
import top.harrylei.forum.service.notify.repository.mapper.NotifyMsgMapper;

import java.util.List;

/**
 * 通知消息访问对象
 *
 * @author harry
 */
@Repository
public class NotifyMsgDAO extends ServiceImpl<NotifyMsgMapper, NotifyMsgDO> {

    /**
     * 根据用户ID分页查询通知列表
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 通知列表
     */
    public Page<NotifyMsgDO> pageByUserId(Long userId, Integer pageNum, Integer pageSize) {
        return lambdaQuery()
                .eq(NotifyMsgDO::getNotifyUserId, userId)
                .orderByDesc(NotifyMsgDO::getId)
                .page(new Page<>(pageNum, pageSize));
    }

    /**
     * 查询用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    public Long countUnreadByUserId(Long userId) {
        return lambdaQuery()
                .eq(NotifyMsgDO::getNotifyUserId, userId)
                .eq(NotifyMsgDO::getState, 0)
                .count();
    }

    /**
     * 标记通知为已读
     *
     * @param notifyId 通知ID
     * @param userId   用户ID（防止越权）
     * @return 是否更新成功
     */
    public boolean markAsRead(Long notifyId, Long userId) {
        return lambdaUpdate()
                .eq(NotifyMsgDO::getId, notifyId)
                .eq(NotifyMsgDO::getNotifyUserId, userId)
                .set(NotifyMsgDO::getState, 1)
                .update();
    }

    /**
     * 标记用户所有通知为已读
     *
     * @param userId 用户ID
     * @return 是否更新成功
     */
    public boolean markAllAsRead(Long userId) {
        return lambdaUpdate()
                .eq(NotifyMsgDO::getNotifyUserId, userId)
                .eq(NotifyMsgDO::getState, 0)
                .set(NotifyMsgDO::getState, 1)
                .update();
    }

    /**
     * 查询用户最近的通知列表（不分页）
     *
     * @param userId 用户ID
     * @param limit  数量限制
     * @return 通知列表
     */
    public List<NotifyMsgDO> listRecentByUserId(Long userId, Integer limit) {
        return lambdaQuery()
                .eq(NotifyMsgDO::getNotifyUserId, userId)
                .orderByDesc(NotifyMsgDO::getId)
                .last("LIMIT " + limit)
                .list();
    }
}