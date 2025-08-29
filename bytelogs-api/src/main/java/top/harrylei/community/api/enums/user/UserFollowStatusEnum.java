package top.harrylei.community.api.enums.user;

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
 * 用户关注状态枚举
 *
 * @author harry
 */
@Getter
@AllArgsConstructor
public enum UserFollowStatusEnum {

    /**
     * 未关注
     */
    UNFOLLOWED(0, "未关注"),

    /**
     * 已关注
     */
    FOLLOWED(1, "已关注");

    // 状态编码（唯一标识）
    @EnumValue
    private final Integer code;

    // 状态描述（用于展示）
    private final String label;

    // 根据状态编码快速定位枚举实例
    private static final Map<Integer, UserFollowStatusEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(UserFollowStatusEnum::getCode, Function.identity()));

    /**
     * 获取状态码
     *
     * @return 状态码
     */
    @JsonValue
    public Integer getCode() {
        return code;
    }

    /**
     * 根据状态编码获取枚举对象
     *
     * @param code 状态编码
     * @return 对应的状态枚举，若无匹配则返回 null
     */
    @JsonCreator
    public static UserFollowStatusEnum fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }
}