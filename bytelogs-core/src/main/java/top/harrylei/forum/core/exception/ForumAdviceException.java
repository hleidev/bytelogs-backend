package top.harrylei.forum.core.exception;

import top.harrylei.forum.api.model.enums.StatusEnum;

import java.io.Serial;

/**
 * 业务通知异常
 * <p>
 * 用于表示需要通知前端的业务状态，不记录错误日志
 * 与 ForumException 的区别在于处理方式不同，但共享相同的数据结构
 */
public class ForumAdviceException extends ForumException {
    @Serial
    private static final long serialVersionUID = -3654231078945766767L;

    /**
     * 通过状态枚举构造通知异常
     *
     * @param statusEnum 状态枚举
     * @param args 消息格式化参数
     */
    public ForumAdviceException(StatusEnum statusEnum, Object... args) {
        super(statusEnum, args);
    }
}
