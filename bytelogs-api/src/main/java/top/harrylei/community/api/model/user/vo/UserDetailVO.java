package top.harrylei.community.api.model.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 用户详情展示对象
 *
 * @author harry
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户详情展示对象")
public class UserDetailVO extends UserListItemVO {

    /**
     * 职位
     */
    @Schema(description = "职位", example = "Java开发工程师")
    private String position;

    /**
     * 公司
     */
    @Schema(description = "公司", example = "Bytelogs Inc.")
    private String company;

    /**
     * 个人简介
     */
    @Schema(description = "个人简介", example = "专注于后端开发与系统架构。")
    private String profile;

    /**
     * 扩展字段（如有自定义属性）
     */
    @Schema(description = "扩展字段", example = "{\"github\":\"https://github.com/harrylei\"}")
    private String extend;
}