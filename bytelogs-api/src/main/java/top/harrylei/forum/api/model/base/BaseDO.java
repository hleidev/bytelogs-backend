package top.harrylei.forum.api.model.base;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础数据对象
 *
 * @author harry
 */
@Data
public class BaseDO implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
