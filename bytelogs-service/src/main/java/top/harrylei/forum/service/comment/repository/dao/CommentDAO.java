package top.harrylei.forum.service.comment.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.forum.service.comment.repository.entity.CommentDO;
import top.harrylei.forum.service.comment.repository.mapper.CommentMapper;

/**
 * 评论访问对象
 *
 * @author harry
 */
@Repository
public class CommentDAO extends ServiceImpl<CommentMapper, CommentDO> {
}