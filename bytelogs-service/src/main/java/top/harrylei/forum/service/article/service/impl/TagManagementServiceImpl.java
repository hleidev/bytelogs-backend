package top.harrylei.forum.service.article.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.StatusEnum;
import top.harrylei.forum.api.model.vo.article.dto.TagDTO;
import top.harrylei.forum.api.model.vo.article.req.TagReq;
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
    }
}
