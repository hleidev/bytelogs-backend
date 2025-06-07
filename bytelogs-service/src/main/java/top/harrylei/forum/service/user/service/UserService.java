package top.harrylei.forum.service.user.service;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import top.harrylei.forum.api.model.vo.page.PageReq;
import top.harrylei.forum.api.model.vo.page.param.UserQueryParam;
import top.harrylei.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import top.harrylei.forum.api.model.vo.user.dto.UserDetailDTO;

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
     * 更新用户信息
     *
     * @param userInfo 需要更新的用户信息DTO
     * @throws RuntimeException 更新失败时抛出异常
     */
    void updateUserInfo(BaseUserInfoDTO userInfo);

    /**
     * 更新用户密码
     * 
     * @param token token
     * @param oldPassword 新密码
     * @param newPassword 旧密码
     */
    void updatePassword(String token, String oldPassword, String newPassword);

    /**
     * 更新用户头像
     * 
     * @param avatar 用户头像
     */
    void updateAvatar(@NotBlank(message = "用户头像不能为空") String avatar);

    /**
     * 用户列表查询
     *
     * @param queryParam 查询参数
     * @param pageRequest 分页参数
     * @return 用户列表
     */
    List<UserDetailDTO> listUsers(UserQueryParam queryParam, PageReq pageRequest);

    /**
     * 统计符合条件的用户数量
     *
     * @param queryParam 查询参数
     * @return 用户数量
     */
    long countUsers(UserQueryParam queryParam);

    /**
     * 查询用户详细信息
     * 
     * @param userId 用户ID
     * @return 用户详细信息
     */
    UserDetailDTO getUserDetail(Long userId);
}
