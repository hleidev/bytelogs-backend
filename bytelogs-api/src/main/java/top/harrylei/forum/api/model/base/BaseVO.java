package top.harrylei.forum.api.model.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * VO 基类，统一通用展示字段
 *
 * @author harry
 */
@Data
public class BaseVO implements Serializable {

    /**
     * 主键ID
     */
    @Schema(description = "主键ID", example = "1")
    private Long id;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 最后编辑时间
     */
    @Schema(description = "最后编辑时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}