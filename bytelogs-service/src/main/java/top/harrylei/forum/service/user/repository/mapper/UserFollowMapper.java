package top.harrylei.forum.service.user.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.harrylei.forum.api.model.vo.user.req.UserFollowQueryParam;
import top.harrylei.forum.api.model.vo.user.vo.UserFollowVO;
import top.harrylei.forum.service.user.repository.entity.UserFollowDO;

import java.util.List;

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
     * @param page       分页参数
     * @return 关注列表
     */
    IPage<UserFollowVO> pageFollowingList(UserFollowQueryParam queryParam, IPage<UserFollowVO> page);

    /**
     * 分页查询用户粉丝列表
     *
     * @param queryParam 查询参数
     * @param page       分页参数
     * @return 粉丝列表
     */
    IPage<UserFollowVO> pageFollowersList(UserFollowQueryParam queryParam, IPage<UserFollowVO> page);

    /**
     * 获取用户的关注者ID列表
     *
     * @param userId 用户ID
     * @return 关注者ID列表
     */
    @Select("SELECT user_id FROM user_relation WHERE follow_user_id = #{userId} AND follow_state = 1 AND deleted = 0")
    List<Long> getFollowerIds(@Param("userId") Long userId);
}