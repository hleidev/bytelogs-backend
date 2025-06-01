package top.harrylei.forum.api.model.exception;

import top.harrylei.forum.api.model.vo.Status;
import top.harrylei.forum.api.model.vo.constants.StatusEnum;
import lombok.Getter;

import java.io.Serial;

/**
 * 业务异常基类
 * <p>
 * 用于表示可预期的业务逻辑异常，会记录警告日志
 */
@Getter
public class ForumException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -7034897190745766929L;

    private final Status status;

    public ForumException(Status status) {
        super(status.getMsg());
        this.status = status;
    }

    public ForumException(int code, String msg) {
        super(msg);
        this.status = Status.newStatus(code, msg);
    }

    public ForumException(StatusEnum statusEnum, Object... args) {
        this.status = Status.newStatus(statusEnum, args);
    }
}
