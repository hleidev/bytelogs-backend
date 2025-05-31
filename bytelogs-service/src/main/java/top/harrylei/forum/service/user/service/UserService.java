package top.harrylei.forum.service.user.service;

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
}
