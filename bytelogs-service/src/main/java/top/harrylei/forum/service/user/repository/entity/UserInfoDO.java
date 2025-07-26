package top.harrylei.forum.service.user.repository.entity;

import java.io.Serial;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.forum.api.model.base.BaseDO;

/**
 * 用户个人信息表
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "user_info")
@Accessors(chain = true)
public class UserInfoDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户图像
     */
    private String avatar;

    /**
     * 职位
     */
    private String position;

    /**
     * 公司
     */
    private String company;

    /**
     * 个人简介
     */
    private String profile;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 删除标记
     */
    private Integer deleted;

    /**
     * 0 普通用户
     * 1 超级管理员
     */
    private Integer userRole;

    /**
     * 用户的邮箱
     */
    private String email;
}
