package top.harrylei.community.service.comment.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import top.harrylei.community.api.model.comment.req.CommentManagementQueryParam;
import top.harrylei.community.api.model.comment.vo.CommentManagementVO;
import top.harrylei.community.service.comment.repository.entity.CommentDO;

/**
 * 评论Mapper接口
 *
 * @author harry
 */
public interface CommentMapper extends BaseMapper<CommentDO> {

    /**
     * 管理端分页查询评论（包含关联数据）
     */
    IPage<CommentManagementVO> pageQueryForManagement(IPage<CommentManagementVO> page, @Param("query") CommentManagementQueryParam query);
}