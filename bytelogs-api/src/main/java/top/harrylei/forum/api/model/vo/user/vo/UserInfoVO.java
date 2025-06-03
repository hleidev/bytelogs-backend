package top.harrylei.forum.api.model.vo.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户信息展示对象（用于前端展示）
 */
@Data
@Schema(description = "用户信息展示对象")
public class UserInfoVO {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "123")
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "harry")
    private String userName;

    /**
     * 用户角色（ADMIN、NORMAL）
     */
    @Schema(description = "角色", example = "ADMIN")
    private String role;

    /**
     * 用户头像URL
     */
    @Schema(description = "头像", example = "https://cdn.bytelogs.com/avatar.jpg")
    private String avatar;

    /**
     * 简介
     */
    @Schema(description = "用户简介", example = "热爱开源和后端开发")
    private String profile;

    /**
     * 职位
     */
    @Schema(description = "职位", example = "后端工程师")
    private String position;

    /**
     * 所属公司
     */
    @Schema(description = "公司", example = "Bytelogs Inc.")
    private String company;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "harry@bytelogs.com")
    private String email;
}