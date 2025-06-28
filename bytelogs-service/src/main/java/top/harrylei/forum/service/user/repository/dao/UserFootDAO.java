package top.harrylei.forum.service.user.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.forum.service.user.repository.entity.UserFootDO;
import top.harrylei.forum.service.user.repository.mapper.UserFootMapper;

/**
 * 用户足迹访问对象
 *
 * @author harry
 */
@Repository
public class UserFootDAO extends ServiceImpl<UserFootMapper, UserFootDO> {

    public UserFootDO getByContentAndUserId(Long userId, Long contentId, Integer type) {
        return lambdaQuery()
                .eq(UserFootDO::getUserId, userId)
                .eq(UserFootDO::getContentId, contentId)
                .eq(UserFootDO::getContentType, type)
                .one();
    }
}