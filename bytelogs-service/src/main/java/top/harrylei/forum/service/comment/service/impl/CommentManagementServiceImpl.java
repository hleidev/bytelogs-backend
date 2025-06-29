package top.harrylei.forum.service.comment.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.harrylei.forum.api.model.vo.comment.req.CommentManagementQueryParam;
import top.harrylei.forum.api.model.vo.comment.vo.CommentManagementVO;
import top.harrylei.forum.api.model.vo.page.PageHelper;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.service.comment.repository.dao.CommentDAO;
import top.harrylei.forum.service.comment.service.CommentManagementService;


/**
 * 评论管理服务实现类
 *
 * @author harry
 */
@Service
@RequiredArgsConstructor
public class CommentManagementServiceImpl implements CommentManagementService {

    private final CommentDAO commentDAO;

    @Override
    public PageVO<CommentManagementVO> pageQuery(CommentManagementQueryParam queryParam) {
        IPage<CommentManagementVO> page = queryParam.toPage();
        IPage<CommentManagementVO> result = commentDAO.pageQueryForManagement(queryParam, page);
        
        return PageHelper.build(result);
    }
}