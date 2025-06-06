package top.harrylei.forum.service.admin.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.StatusEnum;
import top.harrylei.forum.api.model.vo.page.PageHelper;
import top.harrylei.forum.api.model.vo.page.PageReq;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.api.model.vo.page.param.UserQueryParam;
import top.harrylei.forum.api.model.vo.user.vo.UserListItemVO;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.service.admin.service.UserManagementService;
import top.harrylei.forum.service.user.converted.UserStructMapper;
import top.harrylei.forum.service.user.repository.dao.UserDAO;
import top.harrylei.forum.service.user.repository.dao.UserInfoDAO;
import top.harrylei.forum.service.user.repository.entity.UserDO;
import top.harrylei.forum.service.user.repository.entity.UserInfoDO;

/**
 * 用户管理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserDAO userDAO;
    private final UserInfoDAO userInfoDAO;
    private final UserStructMapper userStructMapper;

    /**
     * 分页查询用户列表
     *
     * @param queryParam 查询条件
     * @return 用户分页列表
     */
    @Override
    public PageVO<UserListItemVO> list(UserQueryParam queryParam) {
        try {
            // 创建分页参数
            PageReq pageRequest = PageHelper.createPageRequest(queryParam.getPageNum(), queryParam.getPageSize());

            // 查询用户基础信息
            List<UserDO> users = userDAO.listUsers(queryParam, pageRequest.getLimitSql());
            if (users.isEmpty()) {
                return PageHelper.empty();
            }
            long total = userDAO.count();

            // 查询用户详情信息并构建 Map
            Set<Long> userIds = users.stream().map(UserDO::getId).collect(Collectors.toSet());
            List<UserInfoDO> userInfoList = userInfoDAO.listByIds(userIds);
            Map<Long, UserInfoDO> userInfoMap = userInfoList.stream().collect(
                    Collectors.toMap(UserInfoDO::getUserId, info -> info, (a, b) -> a));

            // 合并信息封装为 VO
            List<UserListItemVO> result = users.stream().map(user -> {
                UserInfoDO userInfo = userInfoMap.get(user.getId());
                return userStructMapper.toUserListItemVO(user, userInfo);
            }).toList();

            // 构建分页结果
            return PageHelper.build(result, pageRequest.getPageNum(), pageRequest.getPageSize(), total);
        } catch (Exception e) {
            log.error("查询用户列表异常", e);
            ExceptionUtil.error(StatusEnum.SYSTEM_ERROR);
            return PageHelper.empty();
        }
    }
}
