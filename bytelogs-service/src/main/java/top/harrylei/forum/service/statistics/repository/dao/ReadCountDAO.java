package top.harrylei.forum.service.statistics.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.forum.service.statistics.repository.entity.ReadCountDO;
import top.harrylei.forum.service.statistics.repository.mapper.ReadCountMapper;

/**
 * 内容访问计数访问对象
 *
 * @author harry
 */
@Repository
public class ReadCountDAO extends ServiceImpl<ReadCountMapper, ReadCountDO> {

    /**
     * 根据内容ID和类型获取访问计数
     *
     * @param contentId   内容ID
     * @param contentType 内容类型
     * @return 访问计数记录
     */
    public ReadCountDO getByContentIdAndType(Long contentId, Integer contentType) {
        return lambdaQuery()
                .eq(ReadCountDO::getContentId, contentId)
                .eq(ReadCountDO::getContentType, contentType)
                .one();
    }

    /**
     * 增加访问计数
     *
     * @param contentId   内容ID
     * @param contentType 内容类型
     * @return 是否成功
     */
    public boolean incrementReadCount(Long contentId, Integer contentType) {
        ReadCountDO existingRecord = getByContentIdAndType(contentId, contentType);
        if (existingRecord == null) {
            return initReadCount(contentId, contentType);
        } else {
            return lambdaUpdate()
                    .eq(ReadCountDO::getContentId, contentId)
                    .eq(ReadCountDO::getContentType, contentType)
                    .setSql("cnt = cnt + 1")
                    .update();
        }
    }

    /**
     * 初始化访问计数记录
     *
     * @param contentId   内容ID
     * @param contentType 内容类型
     * @return 是否成功
     */
    private boolean initReadCount(Long contentId, Integer contentType) {
        ReadCountDO readCount = new ReadCountDO()
                .setContentId(contentId)
                .setContentType(contentType)
                .setCnt(1);
        return save(readCount);
    }

    /**
     * 获取阅读量
     *
     * @param contentId   内容ID
     * @param contentType 内容类型
     * @return 阅读量
     */
    public Long getReadCount(Long contentId, Integer contentType) {
        ReadCountDO readCount = getByContentIdAndType(contentId, contentType);
        return readCount != null ? readCount.getCnt().longValue() : 0L;
    }
}