package top.harrylei.forum.service.user.repository.entity;

import java.io.Serial;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.forum.api.model.entity.BaseDO;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_account")
@Accessors(chain = true)
public class UserDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 第三方用户ID
     */
    private String thirdAccountId;

    /**
     * 登录类型,0:密码登录,1:邮箱验证码登录
     */
    private Integer loginType;

    /**
     * 删除标记
     */
    private Integer deleted;

    /**
     * 登录用户名
     */
    private String userName;

    /**
     * 登录密码，密文存储
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;
}
