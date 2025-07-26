package top.harrylei.forum.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.harrylei.forum.api.enums.base.CodeLabelEnum;
import top.harrylei.forum.api.enums.base.EnumCodeLabelJsonSerializer;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通知类型枚举
 *
 * @author harry
 */
@Getter
@AllArgsConstructor
@JsonSerialize(using = EnumCodeLabelJsonSerializer.class)
public enum NotifyTypeEnum implements CodeLabelEnum {

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
    ARTICLE_PUBLISH(9, "文章发布");

    // 编码（唯一标识）
    @EnumValue
    private final Integer code;

    // 描述（用于展示）
    private final String label;

    // 根据编码快速定位枚举实例
    private static final Map<Integer, NotifyTypeEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(NotifyTypeEnum::getCode, Function.identity()));

    /**
     * 获取编码
     *
     * @return 编码
     */
    @JsonValue
    @Override
    public Integer getCode() {
        return code;
    }

    /**
     * 根据编码获取枚举对象
     *
     * @param code 编码
     * @return 对应的枚举，若无匹配则返回 null
     */
    @JsonCreator
    public static NotifyTypeEnum fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }

    /**
     * 判断是否为系统通知类型
     *
     * @return true-系统通知，false-用户行为通知
     */
    public boolean isSystemNotification() {
        return this == SYSTEM || this == REGISTER || this == LOGIN;
    }
}
