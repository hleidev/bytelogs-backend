package top.harrylei.forum.service.article.repository.dao;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.service.article.repository.entity.TagDO;
import top.harrylei.forum.service.article.repository.mapper.TagMapper;

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
}
