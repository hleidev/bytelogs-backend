package top.harrylei.forum.api.model.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.forum.api.model.base.BaseDTO;
import top.harrylei.forum.api.enums.user.UserRoleEnum;
import top.harrylei.forum.api.enums.YesOrNoEnum;

/**
 * 用户完整信息DTO
 *
 * @author harry
 */
@Data
@Schema(description = "用户完整信息DTO")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserDetailDTO extends BaseDTO {

    /**
     * 用户ID（主键）
     */
    @Schema(description = "用户ID")
    private Long userId;

    // ---------------- user_account 表字段 ----------------

    /**
     * 登录用户名
     */
    @Schema(description = "用户名")
    private String userName;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    private String email;

    /**
     * 启用状态，0-禁用，1-启用
     */
    @Schema(description = "账号状态，0-禁用，1-启用")
    private Integer status;

    /**
     * 删除标记，0-未删除，1-已删除
     */
    @Schema(description = "删除标记，0-未删除，1-已删除")
    private YesOrNoEnum deleted;

    /**
     * 第三方账号ID
     */
    @Schema(description = "第三方账号ID")
    private String thirdAccountId;

    /**
     * 登录类型，0-密码登录，1-邮箱验证码登录
     */
    @Schema(description = "登录类型，0-密码登录，1-邮箱验证码登录")
    private Integer loginType;

    // ---------------- user_info 表字段 ----------------

    /**
     * 用户头像
     */
    @Schema(description = "用户头像")
    private String avatar;

    /**
     * 职位
     */
    @Schema(description = "职位")
    private String position;

    /**
     * 公司
     */
    @Schema(description = "公司")
    private String company;

    /**
     * 个人简介
     */
    @Schema(description = "个人简介")
    private String profile;

    /**
     * 扩展字段（JSON 格式扩展信息）
     */
    @Schema(description = "扩展字段")
    private String extend;

    /**
     * 用户角色，0-普通用户，1-超级管理员
     */
    @Schema(description = "用户角色，0-普通用户，1-超级管理员")
    private UserRoleEnum userRole;
}