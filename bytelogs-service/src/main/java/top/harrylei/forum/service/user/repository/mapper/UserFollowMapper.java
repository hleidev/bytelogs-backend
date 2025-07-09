package top.harrylei.forum.service.user.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import top.harrylei.forum.api.model.vo.user.req.UserFollowQueryParam;
import top.harrylei.forum.api.model.vo.user.vo.UserFollowVO;
import top.harrylei.forum.service.user.repository.entity.UserFollowDO;

/**
 * 用户关注Mapper接口
 *
 * @author harry
 */
public interface UserFollowMapper extends BaseMapper<UserFollowDO> {

    /**
     * 分页查询用户关注列表
     *
     * @param queryParam 查询参数
     * @param page 分页参数
     * @return 关注列表
     */
    IPage<UserFollowVO> pageFollowingList(UserFollowQueryParam queryParam, Page<UserFollowVO> page);

    /**
     * 分页查询用户粉丝列表
     *
     * @param queryParam 查询参数
     * @param page 分页参数
     * @return 粉丝列表
     */
    IPage<UserFollowVO> pageFollowersList(UserFollowQueryParam queryParam, Page<UserFollowVO> page);
}