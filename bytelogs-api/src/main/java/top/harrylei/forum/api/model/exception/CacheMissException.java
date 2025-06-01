package top.harrylei.forum.api.model.exception;

import java.io.Serial;

/**
 * 缓存未命中异常
 * <p>
 * 当在缓存中找不到预期值时抛出，通常用于非关键路径， 重写了 fillInStackTrace 以提高性能
 */
public class CacheMissException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public CacheMissException(String msg) {
        super(msg);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this; // 不收集堆栈信息，提高性能
    }
}