package top.harrylei.forum.api.model.vo.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.forum.api.model.entity.BaseDTO;
import top.harrylei.forum.api.model.enums.user.UserAIStatEnum;

/**
 * @author YiHui
 * @date 2022/8/15
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户基础实体对象")
@Accessors(chain = true)
public class BaseUserInfoDTO extends BaseDTO {
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
    private String role;

    /**
     * 用户图像
     */
    @Schema(description = "用户头像")
    private String photo;
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
    private Integer deleted;

    /**
     * 用户最后登录区域
     */
    @Schema(description = "用户最后登录的地理位置", example = "湖北·武汉")
    private String region;

    /**
     * 星球状态
     */
    private UserAIStatEnum starStatus;

    /**
     * 用户的邮箱
     */
    @Schema(description = "用户邮箱", example = "paicoding@126.com")
    private String email;

    /**
     * 收款码信息
     */
    @Schema(description = "用户的收款码", example = "{\"wx\":\"wxp://f2f0YUXuGn6X2dI6FS2GrMjuG0Lw2plZqwjO4keoZaRr320\"}")
    private String payCode;
}
