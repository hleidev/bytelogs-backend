package top.harrylei.forum.service.user.service;

import java.util.function.Consumer;
import top.harrylei.forum.api.model.vo.user.dto.BaseUserInfoDTO;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息DTO，不存在则返回null
     */
    BaseUserInfoDTO getUserInfoById(Long userId);
    
    /**
     * 异步获取用户信息
     *
     * @param userId 用户ID
     * @param callback 回调函数，用于处理获取到的用户信息
     */
    void getUserInfoAsync(Long userId, Consumer<BaseUserInfoDTO> callback);
}
