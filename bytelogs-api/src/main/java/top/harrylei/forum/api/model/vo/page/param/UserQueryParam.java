package top.harrylei.forum.api.model.vo.page.param;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户列表查询参数
 */
@Data
@Schema(description = "用户列表查询参数")
@Accessors(chain = true)
public class UserQueryParam {

    /**
     * 页码
     */
    @Schema(description = "页码", example = "1", defaultValue = "1")
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    @Schema(description = "每页条数", example = "10", defaultValue = "10")
    private Integer pageSize = 10;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String userName;

    /**
     * 用户状态
     */
    @Schema(description = "用户状态")
    private Integer status;

    /**
     * 删除标识
     */
    @Schema(description = "是否删除")
    private Integer deleted;

    /**
     * 注册起始时间
     */
    @Schema(description = "注册起始时间", example = "2023-01-01 00:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 注册结束时间
     */
    @Schema(description = "注册结束时间", example = "2023-12-31 23:59:59")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}