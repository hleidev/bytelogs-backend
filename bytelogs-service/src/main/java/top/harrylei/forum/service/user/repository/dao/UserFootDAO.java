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

    public UserFootDO getByContentAndUserId(Long contentId, Integer type, Long userId) {
        return lambdaQuery()
                .eq(UserFootDO::getContentId, contentId)
                .eq(UserFootDO::getContentType, type)
                .eq(UserFootDO::getUserId, userId)
                .one();
    }
}