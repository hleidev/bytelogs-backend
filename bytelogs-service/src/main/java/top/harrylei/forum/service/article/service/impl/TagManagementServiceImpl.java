package top.harrylei.forum.service.article.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.StatusEnum;
import top.harrylei.forum.api.model.vo.article.dto.TagDTO;
import top.harrylei.forum.api.model.vo.article.req.TagReq;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.api.model.vo.page.param.TagQueryParam;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.service.article.converted.TagStructMapper;
import top.harrylei.forum.service.article.service.TagManagementService;
import top.harrylei.forum.service.article.service.TagService;

/**
 * 标签管理实现类
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TagManagementServiceImpl implements TagManagementService {

    private final TagService tagService;
    private final TagStructMapper tagStructMapper;

    /**
     * 新建标签
     *
     * @param tagReq 请求参数
     */
    @Override
    public void save(TagReq tagReq) {
        ExceptionUtil.requireNonNull(tagReq, StatusEnum.PARAM_MISSING, "请求参数");

        TagDTO tag = tagStructMapper.toDTO(tagReq);
        tagService.save(tag);
        log.info("新建标签成功 tagName={} categoryId={} operatorId={}", tagReq.getTagName(), tag.getCategoryId(),
            ReqInfoContext.getContext().getUserId());
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

        return tagService.page(queryParam);
    }

    /**
     * 编辑标签
     *
     * @param tagId 标签ID
     * @param tagReq 标签编辑请求
     * @return 标签详细信息
     */
    @Override
    public TagDTO update(Long tagId, TagReq tagReq) {
        ExceptionUtil.requireNonNull(tagId, StatusEnum.PARAM_MISSING, "标签ID");
        ExceptionUtil.requireNonNull(tagReq, StatusEnum.PARAM_MISSING, "标签编辑请求");

        TagDTO tagDTO = tagStructMapper.toDTO(tagReq);
        tagDTO.setId(tagId);

        TagDTO tag = tagService.update(tagDTO);
        log.info("修改标签成功 tagId={} operatorId={}", tagId, ReqInfoContext.getContext().getUserId());
        return tag;
    }

    /**
     * 删除标签
     *
     * @param tagId 标签ID
     */
    @Override
    public void delete(Long tagId) {
        tagService.delete(tagId);
        log.info("删除标签成功 tagId={} operatorId={}", tagId, ReqInfoContext.getContext().getUserId());
    }
}
