package top.harrylei.community.api.model.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户关注展示对象
 *
 * @author harry
 */
@Data
@Accessors(chain = true)
@Schema(description = "用户关注展示对象")
public class UserFollowVO {

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
     * 用户头像
     */
    @Schema(description = "用户头像", example = "https://cdn.bytelogs.com/avatar.jpg")
    private String avatar;

    /**
     * 个人简介
     */
    @Schema(description = "个人简介", example = "专注于后端开发与系统架构")
    private String profile;

    /**
     * 是否已关注
     */
    @Schema(description = "是否已关注", example = "true")
    private Boolean followed;

    /**
     * 粉丝数
     */
    @Schema(description = "粉丝数", example = "1024")
    private Long fanCount;

    /**
     * 关注数
     */
    @Schema(description = "关注数", example = "256")
    private Long followCount;

    /**
     * 关注时间
     */
    @Schema(description = "关注时间", example = "2023-04-01T12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime followTime;
}