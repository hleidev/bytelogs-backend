package top.harrylei.forum.api.model.vo.user.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户信息更新请求
 */
@Data
@Schema(description = "用户信息更新请求")
@Accessors(chain = true)
public class UserInfoReq {
    /**
     * 用户名
     */
    @NotBlank
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userName;

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
}
