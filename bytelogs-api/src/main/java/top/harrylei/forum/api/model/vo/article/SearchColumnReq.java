package top.harrylei.forum.api.model.vo.article;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "教程查询")
public class SearchColumnReq {

    // 教程名称
    @Schema(description = "教程名称")
    private String column;

    @Schema(description = "请求页数，从1开始计数")
    private long pageNumber;

    @Schema(description = "请求页大小，默认为 10")
    private long pageSize;
}
