package top.harrylei.forum.service.article.service;

import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.enums.article.PublishStatusEnum;
import top.harrylei.forum.api.model.vo.article.dto.TagDTO;
import top.harrylei.forum.api.model.vo.article.vo.TagSimpleVO;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.api.model.vo.page.param.TagQueryParam;

import java.util.List;

/**
 * 标签服务接口类
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
    PageVO<TagDTO> page(TagQueryParam queryParam);

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
     * @param tagId 标签ID
     * @param yesOrNoEnum 删除标识
     */
    void updateDelete(Long tagId, YesOrNoEnum yesOrNoEnum);

    /**
     * 已删标签
     *
     * @return 已经删除的标签详细信息列表
     */
    List<TagDTO> listDeleted();

    /**
     * 修改状态
     *
     * @param tagId 标签ID
     * @param status 发布状态
     */
    void updateStatus(Long tagId, PublishStatusEnum status);

    /**
     * 标签列表
     *
     * @return 返回标签简单展示对象
     */
    List<TagSimpleVO> listSimpleTags();
}
