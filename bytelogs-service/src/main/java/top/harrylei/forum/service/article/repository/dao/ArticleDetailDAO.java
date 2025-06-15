package top.harrylei.forum.service.article.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.forum.service.article.repository.entity.ArticleDetailDO;
import top.harrylei.forum.service.article.repository.mapper.ArticleDetailMapper;

/**
 * 文章详细访问对象
 */
@Repository
public class ArticleDetailDAO extends ServiceImpl<ArticleDetailMapper, ArticleDetailDO> {

}