package top.harrylei.forum.service.article.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.StatusEnum;
import top.harrylei.forum.api.model.vo.article.dto.TagDTO;
import top.harrylei.forum.api.model.vo.page.Page;
import top.harrylei.forum.api.model.vo.page.PageHelper;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.api.model.vo.page.param.TagQueryParam;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.service.article.converted.TagStructMapper;
import top.harrylei.forum.service.article.repository.dao.TagDAO;
import top.harrylei.forum.service.article.repository.entity.TagDO;
import top.harrylei.forum.service.article.service.TagService;

/**
 * 标签服务实现类
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TagServiceImpl implements TagService {

    private final TagDAO tagDAO;
    private final TagStructMapper tagStructMapper;

    /**
     * 新建标签
     *
     * @param tag 标签传输对象
     */
    @Override
    public void save(TagDTO tag) {
        ExceptionUtil.requireNonNull(tag, StatusEnum.PARAM_MISSING, "标签");

        TagDO tagDO = tagStructMapper.toDO(tag);
        boolean hasTag = tagDAO.existsTag(tagDO);
        ExceptionUtil.errorIf(hasTag, StatusEnum.Tag_EXISTS, "tag=" + tag);

        try {
            tagDAO.save(tagDO);
        } catch (Exception e) {
            ExceptionUtil.error(StatusEnum.SYSTEM_ERROR, e);
        }
    }

    /**
     * 标签分页查询
     *
     * @param queryParam 标签及筛选参数
     * @return 标签分类列表
     */
    @Override
    public PageVO<TagDTO> page(TagQueryParam queryParam) {
        ExceptionUtil.requireNonNull(queryParam, StatusEnum.PARAM_MISSING, "分页请求参数");

        Page page = PageHelper.createPage(queryParam.getPageNum(), queryParam.getPageSize());

        List<TagDO> tags = tagDAO.listTags(queryParam, page);
        List<TagDTO> result = tags.stream()
                .filter(Objects::nonNull)
                .map(tagStructMapper::toDTO).toList();

        long total = tagDAO.countTags(queryParam);

        return PageHelper.build(result, page.getPageNum(), page.getPageSize(), total);
    }

    /**
     * 更新标签
     *
     * @param tagDTO 标签传输对象
     * @return 更新后的标签传输对象
     */
    @Override
    public TagDTO update(TagDTO tagDTO) {
        ExceptionUtil.requireNonNull(tagDTO, StatusEnum.PARAM_MISSING, "标签传输对象");

        TagDO tagDO = tagDAO.getByTagId(tagDTO.getId());
        ExceptionUtil.requireNonNull(tagDO, StatusEnum.Tag_NOT_EXISTS, "tagId=" + tagDTO.getId());

        tagStructMapper.updateTagDOFromTagDTO(tagDTO, tagDO);
        tagDAO.updateById(tagDO);
        return tagStructMapper.toDTO(tagDO);
    }
}
