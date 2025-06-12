package top.harrylei.forum.service.article.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.StatusEnum;
import top.harrylei.forum.api.model.vo.article.dto.TagDTO;
import top.harrylei.forum.core.context.ReqInfoContext;
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

        Long operatorId = ReqInfoContext.getContext().getUserId();

        try {
            tagDAO.save(tagDO);
            log.info("新建标签成功 tagName={} categoryId={} operatorId={}", tag.getTagName(), tag.getCategoryId(), operatorId);
        } catch (Exception e) {
            ExceptionUtil.error(StatusEnum.SYSTEM_ERROR, e);
        }
    }
}
