package top.harrylei.community.service.article.repository.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.api.model.page.param.TagQueryParam;
import top.harrylei.community.service.article.repository.entity.TagDO;
import top.harrylei.community.service.article.repository.mapper.TagMapper;

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
                .eq(TagDO::getDeleted, DeleteStatusEnum.NOT_DELETED)
                .one() != null;
    }

    public IPage<TagDO> pageQuery(TagQueryParam queryParam, IPage<TagDO> page, boolean deleted) {
        DeleteStatusEnum deleteStatus = deleted ? DeleteStatusEnum.DELETED : DeleteStatusEnum.NOT_DELETED;
        return lambdaQuery()
                .like(queryParam.getTagName() != null && !queryParam.getTagName().isEmpty(),
                        TagDO::getTagName, queryParam.getTagName())
                .eq(queryParam.getTagType() != null, TagDO::getTagType, queryParam.getTagType())
                .ge(queryParam.getStartTime() != null, TagDO::getCreateTime, queryParam.getStartTime())
                .le(queryParam.getEndTime() != null, TagDO::getCreateTime, queryParam.getEndTime())
                .eq(TagDO::getDeleted, deleteStatus)
                .page(page);
    }

    public TagDO getByTagId(Long id) {
        return lambdaQuery()
                .eq(TagDO::getId, id)
                .eq(TagDO::getDeleted, DeleteStatusEnum.NOT_DELETED)
                .one();
    }

    public List<TagDO> listSimpleTag() {
        return lambdaQuery()
                .eq(TagDO::getDeleted, DeleteStatusEnum.NOT_DELETED)
                .orderByAsc(TagDO::getTagName)
                .list();
    }

    /**
     * 标签搜索
     *
     * @param keyword 搜索关键词
     * @return 匹配的标签列表
     */
    public List<TagDO> searchByKeyword(String keyword) {
        return lambdaQuery()
                .eq(TagDO::getDeleted, DeleteStatusEnum.NOT_DELETED)
                .like(TagDO::getTagName, keyword)
                .orderByAsc(TagDO::getTagName)
                .list();
    }

    /**
     * 根据标签名查找标签
     *
     * @param tagName 标签名称
     * @return 标签对象，未找到时返回null
     */
    public TagDO getByTagName(String tagName) {
        return lambdaQuery()
                .eq(TagDO::getTagName, tagName)
                .eq(TagDO::getDeleted, DeleteStatusEnum.NOT_DELETED)
                .one();
    }
}
