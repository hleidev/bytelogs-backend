package top.harrylei.forum.api.model.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知类型枚举
 *
 * @author harry
 */

@Getter
public enum NotifyTypeEnum {
    // 用户互动通知（正向行为，值得通知）
    COMMENT(1, "评论"),
    REPLY(2, "回复"),
    PRAISE(3, "点赞"),
    COLLECT(4, "收藏"),
    FOLLOW(5, "关注"),

    // 系统通知
    SYSTEM(6, "系统消息"),
    REGISTER(7, "用户注册"),
    LOGIN(8, "用户登录"),

    // 扩展通知类型（预留）
    ARTICLE_PUBLISH(9, "文章发布"),
    COMMENT_REPLY(10, "评论被回复"),
    ;


    /**
     * 表示消息类型： 1-6 对应的时评论/回复/点赞/关注消息/系统消息等
     */
    private int type;
    private String msg;

    private static Map<Integer, NotifyTypeEnum> mapper;

    static {
        mapper = new HashMap<>();
        for (NotifyTypeEnum type : values()) {
            mapper.put(type.type, type);
        }
    }

    NotifyTypeEnum(int type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public static NotifyTypeEnum typeOf(int type) {
        return mapper.get(type);
    }

    public static NotifyTypeEnum typeOf(String type) {
        return valueOf(type.toUpperCase().trim());
    }
}
