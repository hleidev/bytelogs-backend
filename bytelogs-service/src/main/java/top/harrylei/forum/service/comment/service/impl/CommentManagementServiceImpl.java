package top.harrylei.forum.service.comment.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.harrylei.forum.api.model.vo.comment.req.CommentManagementQueryParam;
import top.harrylei.forum.api.model.vo.comment.vo.CommentManagementVO;
import top.harrylei.forum.api.model.vo.page.PageHelper;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.service.comment.repository.dao.CommentDAO;
import top.harrylei.forum.service.comment.service.CommentManagementService;
import top.harrylei.forum.service.comment.service.CommentService;

import java.util.List;


/**
 * 评论管理服务实现类
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentManagementServiceImpl implements CommentManagementService {

    private final CommentDAO commentDAO;
    private final CommentService commentService;

    @Override
    public PageVO<CommentManagementVO> pageQuery(CommentManagementQueryParam queryParam) {
        IPage<CommentManagementVO> page = queryParam.toPage();
        IPage<CommentManagementVO> result = commentDAO.pageQueryForManagement(queryParam, page);
        
        return PageHelper.build(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComments(List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            log.warn("删除评论列表为空，无需处理");
            return;
        }

        log.info("管理员批量删除评论，commentIds={}", commentIds);
        
        for (Long commentId : commentIds) {
            try {
                commentService.deleteComment(commentId);
                log.info("成功删除评论，commentId={}", commentId);
            } catch (Exception e) {
                log.error("删除评论失败，commentId={}，错误信息：{}", commentId, e.getMessage(), e);
                throw e;
            }
        }
        
        log.info("批量删除评论完成，总数={}", commentIds.size());
    }
}