package top.harrylei.forum.service.article.repository.dao;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import top.harrylei.forum.service.article.repository.entity.ArticleTagDO;
import top.harrylei.forum.service.article.repository.mapper.ArticleTagMapper;

/**
 * 文章标签关系访问对象
 */
@Repository
public class ArticleTagDAO extends ServiceImpl<ArticleTagMapper, ArticleTagDO> {

}
