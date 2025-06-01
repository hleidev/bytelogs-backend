package top.harrylei.forum.api.model.exception;

import top.harrylei.forum.api.model.vo.Status;
import top.harrylei.forum.api.model.vo.constants.StatusEnum;

import java.io.Serial;

/**
 * 业务通知异常
 * <p>
 * 用于表示需要通知前端的业务状态，不记录错误日志 与 ForumException 的区别在于处理方式不同，但共享相同的数据结构
 */
public class ForumAdviceException extends ForumException {
    @Serial
    private static final long serialVersionUID = -3654231078945766767L;

    public ForumAdviceException(Status status) {
        super(status);
    }

    public ForumAdviceException(int code, String msg) {
        super(code, msg);
    }

    public ForumAdviceException(StatusEnum statusEnum, Object... args) {
        super(statusEnum, args);
    }
}
