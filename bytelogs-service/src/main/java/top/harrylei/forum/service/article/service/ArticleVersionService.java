package top.harrylei.forum.service.article.service;

import top.harrylei.forum.api.model.vo.article.vo.ArticleVO;
import top.harrylei.forum.api.model.vo.article.vo.ArticleVersionVO;

import java.util.List;

/**
 * 文章版本管理服务接口
 *
 * @author harry
 */
public interface ArticleVersionService {

    /**
     * 获取文章版本历史
     *
     * @param articleId 文章ID
     * @return 版本历史列表，按版本号降序排列
     */
    List<ArticleVersionVO> getVersionHistory(Long articleId);

    /**
     * 获取特定版本详情
     *
     * @param articleId 文章ID
     * @param version   版本号
     * @return 版本详情
     */
    ArticleVO getVersionDetail(Long articleId, Integer version);
}