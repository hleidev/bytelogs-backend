package top.harrylei.forum.api.model.vo.user.vo;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户列表项展示对象
 * <p>
 * 用于管理界面的用户列表展示，包含用户账号和基本信息
 */
@Data
@Schema(description = "用户列表项展示对象")
@Accessors(chain = true)
public class UserListItemVO {

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
     * 用户状态
     */
    @Schema(description = "账号状态：禁用，启用", example = "启用")
    private String status;

    /**
     * 用户角色
     */
    @Schema(description = "角色代码", example = "管理员")
    private String role;

    /**
     * 用户头像URL
     */
    @Schema(description = "头像", example = "https://cdn.bytelogs.com/avatar.jpg")
    private String avatar;

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

    /**
     * 简介
     */
    @Schema(description = "用户简介", example = "热爱开源和后端开发")
    private String profile;

    /**
     * 删除标记
     */
    @Schema(description = "是否已删除", example = "未删除")
    private String deleted;

    /**
     * 创建时间
     */
    @Schema(description = "注册时间", example = "2023-04-01T12:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    @Schema(description = "最后更新时间", example = "2023-04-01T15:30:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}