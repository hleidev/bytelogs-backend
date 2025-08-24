package top.harrylei.forum.service.article.service;

import top.harrylei.forum.api.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.article.dto.TagDTO;
import top.harrylei.forum.api.model.article.vo.TagSimpleVO;
import top.harrylei.forum.api.model.page.PageVO;
import top.harrylei.forum.api.model.page.param.TagQueryParam;

import java.util.List;

/**
 * 标签服务接口类
 *
 * @author harry
 */
public interface TagService {

    /**
     * 新建标签
     *
     * @param tag 标签传输对象
     */
    void save(TagDTO tag);

    /**
     * 标签分页查询
     *
     * @param queryParam 标签及筛选参数
     * @return 标签分类列表
     */
    PageVO<TagDTO> pageQuery(TagQueryParam queryParam);

    /**
     * 标签分页查询
     *
     * @param queryParam 标签及筛选参数
     * @param deleted    是否查询已删除标签
     * @return 标签分类列表
     */
    PageVO<TagDTO> pageQuery(TagQueryParam queryParam, boolean deleted);

    /**
     * 更新标签
     *
     * @param tagDTO 标签传输对象
     * @return 更新后的标签传输对象
     */
    TagDTO update(TagDTO tagDTO);

    /**
     * 更新标签
     *
     * @param tagId       标签ID
     * @param yesOrNoEnum 删除标识
     */
    void updateDelete(Long tagId, YesOrNoEnum yesOrNoEnum);

    /**
     * 标签列表
     *
     * @return 返回标签简单展示对象
     */
    List<TagSimpleVO> listSimpleTags();

    /**
     * 标签搜索
     *
     * @param keyword 搜索关键词
     * @return 匹配的标签列表
     */
    List<TagSimpleVO> searchTags(String keyword);

    /**
     * 创建或获取标签
     *
     * @param tagName 标签名称
     * @param userId  用户ID
     * @return 标签ID
     */
    Long createIfAbsent(String tagName, Long userId);
}
