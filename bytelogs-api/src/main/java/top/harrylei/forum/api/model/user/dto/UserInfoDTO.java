package top.harrylei.forum.api.model.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.forum.api.model.base.BaseDTO;
import top.harrylei.forum.api.enums.user.UserRoleEnum;
import top.harrylei.forum.api.enums.YesOrNoEnum;

/**
 * 用户基础实体对象
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户基础实体对象")
@Accessors(chain = true)
public class UserInfoDTO extends BaseDTO {
    /**
     * 用户id
     */
    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userName;

    /**
     * 用户角色 admin, normal
     */
    @Schema(description = "角色", example = "ADMIN|NORMAL")
    private UserRoleEnum role;

    /**
     * 用户图像
     */
    @Schema(description = "用户头像")
    private String avatar;
    /**
     * 个人简介
     */
    @Schema(description = "用户简介")
    private String profile;
    /**
     * 职位
     */
    @Schema(description = "个人职位")
    private String position;

    /**
     * 公司
     */
    @Schema(description = "公司")
    private String company;

    /**
     * 扩展字段
     */
    @Schema(hidden = true)
    private String extend;

    /**
     * 是否删除
     */
    @Schema(hidden = true, description = "用户是否被删除")
    private YesOrNoEnum deleted;

    /**
     * 用户的邮箱
     */
    @Schema(description = "用户邮箱", example = "bytelogs@gmail.com")
    private String email;
}
