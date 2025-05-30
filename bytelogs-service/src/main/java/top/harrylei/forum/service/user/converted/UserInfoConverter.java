package top.harrylei.forum.service.user.converted;

import top.harrylei.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import top.harrylei.forum.service.user.repository.entity.UserInfoDO;

public class UserInfoConverter {

    public static BaseUserInfoDTO toDTO(UserInfoDO userInfo) {
        if (userInfo == null) {
            return null;
        }

        BaseUserInfoDTO dto = new BaseUserInfoDTO()
                .setUserId(userInfo.getUserId())
                .setUserName(userInfo.getUserName())
                .setPhoto(userInfo.getPhoto())
                .setProfile(userInfo.getProfile())
                .setPosition(userInfo.getPosition())
                .setCompany(userInfo.getCompany())
                .setEmail(userInfo.getEmail());

        // 设置角色
        if (userInfo.getUserRole() != null && userInfo.getUserRole() == 1) {
            dto.setRole("ADMIN");
        } else {
            dto.setRole("NORMAL");
        }

        return dto;
    }
}
