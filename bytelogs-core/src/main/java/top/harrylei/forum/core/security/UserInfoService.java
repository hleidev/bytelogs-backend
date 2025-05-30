package top.harrylei.forum.core.security;

import top.harrylei.forum.api.model.vo.user.dto.BaseUserInfoDTO;

import java.util.function.Consumer;

/**
 * 用户信息服务接口，用于获取用户详细信息
 * 设计为接口以解耦实现，避免循环依赖
 */
public interface UserInfoService {

    /**
     * 根据用户ID获取用户详细信息
     *
     * @param userId 用户ID
     * @return 用户详细信息，不存在则返回null
     */
    BaseUserInfoDTO getUserInfo(Long userId);

    /**
     * 异步加载用户信息
     *
     * @param userId 用户ID
     * @param callback 加载完成回调
     */
    void loadUserInfo(Long userId, Consumer<BaseUserInfoDTO> callback);
}