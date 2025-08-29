package top.harrylei.community.api.enums.notify;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通知消息状态枚举
 *
 * @author harry
 */
@Getter
@AllArgsConstructor
public enum NotifyMsgStateEnum {

    UNREAD(0, "未读"),
    READ(1, "已读");

    // 编码（唯一标识）
    @EnumValue
    private final Integer code;

    // 描述（用于展示）
    private final String label;

    // 根据编码快速定位枚举实例
    private static final Map<Integer, NotifyMsgStateEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(NotifyMsgStateEnum::getCode, Function.identity()));

    /**
     * 获取编码
     *
     * @return 编码
     */
    @JsonValue
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
    public static NotifyMsgStateEnum fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }
}