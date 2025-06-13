package top.harrylei.forum.service.article.repository.dao;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.vo.page.Page;
import top.harrylei.forum.api.model.vo.page.param.TagQueryParam;
import top.harrylei.forum.service.article.repository.entity.TagDO;
import top.harrylei.forum.service.article.repository.mapper.TagMapper;

import java.util.List;

/**
 * 标签访问对象
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

    public List<TagDO> listTags(TagQueryParam queryParam, Page page) {
        String orderBySql = queryParam.getOrderBySql();
        return lambdaQuery()
                .like(queryParam.getTagName() != null && !queryParam.getTagName().isEmpty(),
                        TagDO::getTagName, queryParam.getTagName())
                .eq(queryParam.getTagType() != null, TagDO::getTagType, queryParam.getTagType())
                .eq(queryParam.getCategoryId() != null, TagDO::getCategoryId, queryParam.getCategoryId())
                .eq(queryParam.getStatus() != null, TagDO::getStatus, queryParam.getStatus())
                .eq(TagDO::getDeleted, YesOrNoEnum.NO.getCode())
                .last(orderBySql + " " + page.getLimitSql())
                .list();
    }

    public Long countTags(TagQueryParam queryParam) {
        return lambdaQuery()
                .like(queryParam.getTagName() != null && !queryParam.getTagName().isEmpty(),
                        TagDO::getTagName, queryParam.getTagName())
                .eq(queryParam.getTagType() != null, TagDO::getTagType, queryParam.getTagType())
                .eq(queryParam.getCategoryId() != null, TagDO::getCategoryId, queryParam.getCategoryId())
                .eq(queryParam.getStatus() != null, TagDO::getStatus, queryParam.getStatus())
                .eq(TagDO::getDeleted, YesOrNoEnum.NO.getCode())
                .count();
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
}
