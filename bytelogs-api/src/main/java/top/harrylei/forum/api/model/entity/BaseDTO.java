package top.harrylei.forum.api.model.entity;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BaseDTO {
    @Schema(description = "业务主键")
    private Long id;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "最后编辑时间")
    private Date updateTime;
}
