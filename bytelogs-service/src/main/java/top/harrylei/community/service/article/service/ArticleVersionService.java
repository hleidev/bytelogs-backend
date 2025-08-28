package top.harrylei.community.service.article.service;

import top.harrylei.community.api.model.article.vo.ArticleVO;
import top.harrylei.community.api.model.article.vo.ArticleVersionVO;
import top.harrylei.community.api.model.article.vo.VersionDiffVO;

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

    /**
     * 对比两个版本
     *
     * @param articleId 文章ID
     * @param version1  版本1
     * @param version2  版本2
     * @return 版本对比结果
     */
    VersionDiffVO compareVersions(Long articleId, Integer version1, Integer version2);
}