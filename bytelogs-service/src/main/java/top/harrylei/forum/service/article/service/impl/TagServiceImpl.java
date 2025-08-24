package top.harrylei.forum.service.article.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import top.harrylei.forum.api.enums.ResultCode;
import top.harrylei.forum.api.enums.YesOrNoEnum;
import top.harrylei.forum.api.enums.article.TagTypeEnum;
import top.harrylei.forum.api.model.article.dto.TagDTO;
import top.harrylei.forum.api.model.article.vo.TagSimpleVO;
import top.harrylei.forum.api.model.page.PageVO;
import top.harrylei.forum.api.model.page.param.TagQueryParam;
import top.harrylei.forum.core.util.PageUtils;
import top.harrylei.forum.service.article.converted.TagStructMapper;
import top.harrylei.forum.service.article.repository.dao.TagDAO;
import top.harrylei.forum.service.article.repository.entity.TagDO;
import top.harrylei.forum.service.article.service.TagService;

import java.util.List;
import java.util.Objects;

/**
 * 标签服务实现类
 *
 * @author harry
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
        if (tag == null || tag.getTagName() == null || tag.getTagName().trim().isEmpty()) {
            ResultCode.INVALID_PARAMETER.throwException("标签参数不能为空");
        }

        TagDO tagDO = tagStructMapper.toDO(tag);
        boolean hasTag = tagDAO.existsTag(tagDO);
        if (hasTag) {
            ResultCode.TAG_ALREADY_EXISTS.throwException();
        }

        try {
            tagDAO.save(tagDO);
        } catch (Exception e) {
            ResultCode.INTERNAL_ERROR.throwException();
        }
    }

    @Override
    public PageVO<TagDTO> pageQuery(TagQueryParam queryParam) {
        return pageQuery(queryParam, false);
    }

    /**
     * 标签分页查询
     *
     * @param queryParam 标签及筛选参数
     * @return 标签列表
     */
    @Override
    public PageVO<TagDTO> pageQuery(TagQueryParam queryParam, boolean deleted) {
        if (queryParam == null) {
            ResultCode.INVALID_PARAMETER.throwException("分页请求参数不能为空");
        }

        // 使用新的MyBatis-Plus分页
        IPage<TagDO> page = PageUtils.of(queryParam);
        IPage<TagDO> result = tagDAO.pageQuery(queryParam, page, deleted);

        // 转换为DTO并构建返回结果
        return PageUtils.from(result, tagStructMapper::toDTO);
    }

    /**
     * 更新标签
     *
     * @param tagDTO 标签传输对象
     * @return 更新后的标签传输对象
     */
    @Override
    public TagDTO update(TagDTO tagDTO) {
        if (tagDTO == null || tagDTO.getId() == null) {
            ResultCode.INVALID_PARAMETER.throwException("标签传输对象或ID不能为空");
        }

        TagDO tagDO = tagDAO.getByTagId(tagDTO.getId());
        if (tagDO == null) {
            ResultCode.TAG_NOT_EXISTS.throwException();
        }

        // 手动更新可编辑字段，保持ID和审计字段不变
        tagDO.setTagName(tagDTO.getTagName());
        tagDO.setTagType(tagDTO.getTagType().getCode());
        tagDAO.updateById(tagDO);
        return tagStructMapper.toDTO(tagDO);
    }

    /**
     * 更新标签
     *
     * @param tagId       标签ID
     * @param yesOrNoEnum 删除标识
     */
    @Override
    public void updateDelete(Long tagId, YesOrNoEnum yesOrNoEnum) {
        TagDO tag = tagDAO.getById(tagId);
        if (tag == null) {
            ResultCode.TAG_NOT_EXISTS.throwException();
        }

        if (Objects.equals(tag.getDeleted(), yesOrNoEnum.getCode())) {
            log.warn("标签删除状态未变更，无需更新");
            return;
        }

        tag.setDeleted(yesOrNoEnum.getCode());
        tagDAO.updateById(tag);
    }

    /**
     * 标签列表
     *
     * @return 标签简单展示对象列表
     */
    @Override
    public List<TagSimpleVO> listSimpleTags() {
        List<TagDO> tags = tagDAO.listSimpleTag();
        return convertToSimpleVOList(tags);
    }

    @Override
    public List<TagSimpleVO> searchTags(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return List.of();
        }
        List<TagDO> tags = tagDAO.searchByKeyword(keyword);
        return convertToSimpleVOList(tags);
    }

    @Override
    public Long createIfAbsent(String tagName, Long userId) {
        // 先查找现有标签
        TagDO tag = tagDAO.getByTagName(tagName);
        if (tag != null) {
            return tag.getId();
        }

        // 创建新标签
        TagDO newTag = new TagDO()
                .setTagName(tagName)
                .setTagType(TagTypeEnum.USER.getCode())
                .setCreatorId(userId)
                .setDeleted(YesOrNoEnum.NO.getCode());

        tagDAO.save(newTag);
        return newTag.getId();
    }

    /**
     * 转换标签DO列表为简单VO列表
     *
     * @param tags 标签DO列表
     * @return 标签简单VO列表
     */
    private List<TagSimpleVO> convertToSimpleVOList(List<TagDO> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .filter(Objects::nonNull)
                .map(tagStructMapper::toSimpleVO)
                .toList();
    }
}
