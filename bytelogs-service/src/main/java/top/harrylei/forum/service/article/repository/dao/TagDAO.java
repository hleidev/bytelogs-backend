package top.harrylei.forum.service.article.repository.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.forum.api.enums.YesOrNoEnum;
import top.harrylei.forum.api.enums.article.PublishStatusEnum;
import top.harrylei.forum.api.model.page.param.TagQueryParam;
import top.harrylei.forum.service.article.repository.entity.TagDO;
import top.harrylei.forum.service.article.repository.mapper.TagMapper;

import java.util.List;

/**
 * 标签访问对象
 *
 * @author harry
 */
@Repository
public class TagDAO extends ServiceImpl<TagMapper, TagDO> {

    public boolean existsTag(TagDO tag) {
        return lambdaQuery()
                .eq(TagDO::getTagName, tag.getTagName())
                .eq(TagDO::getTagType, tag.getTagType())
                .eq(TagDO::getCategoryId, tag.getCategoryId())
                .eq(TagDO::getStatus, tag.getStatus())
                .eq(TagDO::getDeleted, YesOrNoEnum.NO.getCode())
                .one() != null;
    }

    public IPage<TagDO> pageQuery(TagQueryParam queryParam, IPage<TagDO> page) {
        return lambdaQuery()
                .like(queryParam.getTagName() != null && !queryParam.getTagName().isEmpty(),
                      TagDO::getTagName, queryParam.getTagName())
                .eq(queryParam.getTagType() != null, TagDO::getTagType, queryParam.getTagType())
                .eq(queryParam.getCategoryId() != null, TagDO::getCategoryId, queryParam.getCategoryId())
                .eq(queryParam.getStatus() != null, TagDO::getStatus, queryParam.getStatus())
                .ge(queryParam.getStartTime() != null, TagDO::getCreateTime, queryParam.getStartTime())
                .le(queryParam.getEndTime() != null, TagDO::getCreateTime, queryParam.getEndTime())
                .eq(TagDO::getDeleted, YesOrNoEnum.NO.getCode())
                .page(page);
    }

    public TagDO getByTagId(Long id) {
        return lambdaQuery()
                .eq(TagDO::getId, id)
                .eq(TagDO::getDeleted, YesOrNoEnum.NO.getCode())
                .one();
    }

    public List<TagDO> listDeleted() {
        return lambdaQuery()
                .eq(TagDO::getDeleted, YesOrNoEnum.YES.getCode())
                .list();
    }

    public List<TagDO> listPublishedAndUndeleted() {
        return lambdaQuery()
                .eq(TagDO::getStatus, PublishStatusEnum.PUBLISHED.getCode())
                .eq(TagDO::getDeleted, YesOrNoEnum.NO.getCode())
                .list();
    }
}
