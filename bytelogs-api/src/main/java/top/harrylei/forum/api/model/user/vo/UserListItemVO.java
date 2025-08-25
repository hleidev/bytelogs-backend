package top.harrylei.forum.api.model.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import top.harrylei.forum.api.enums.user.UserRoleEnum;
import top.harrylei.forum.api.enums.YesOrNoEnum;

/**
 * 用户列表项展示对象
 *
 * @author harry
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
    @Schema(description = "账号状态：0-禁用，1-启用", example = "1")
    private Integer status;

    /**
     * 用户角色
     */
    @Schema(description = "角色", example = "ADMIN")
    private UserRoleEnum role;

    /**
     * 用户头像URL
     */
    @Schema(description = "头像", example = "https://cdn.bytelogs.com/avatar.jpg")
    private String avatar;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "harry@bytelogs.com")
    private String email;

    /**
     * 删除标记
     */
    @Schema(description = "是否已删除", example = "NO")
    private YesOrNoEnum deleted;

    /**
     * 创建时间
     */
    @Schema(description = "注册时间", example = "2023-04-01T12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    @Schema(description = "最后更新时间", example = "2023-04-01T15:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}