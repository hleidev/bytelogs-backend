package top.harrylei.forum.api.enums.user;

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
 * 用户关注状态枚举
 *
 * @author harry
 */
@Getter
@AllArgsConstructor
@JsonSerialize(using = EnumCodeLabelJsonSerializer.class)
public enum UserFollowStatusEnum implements CodeLabelEnum {

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
    // 根据枚举名称（不区分大小写）快速定位枚举实例
    private static final Map<String, UserFollowStatusEnum> NAME_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(e -> e.name().toUpperCase(), Function.identity()));

    /**
     * 获取状态码
     *
     * @return 状态码
     */
    @Override
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

    /**
     * 根据状态编码获取状态标签，若未找到则返回默认值
     *
     * @param code         状态编码
     * @param defaultLabel 默认值
     * @return 状态描述，若无匹配则返回默认值
     */
    public static String getLabelByCode(Integer code, String defaultLabel) {
        UserFollowStatusEnum status = fromCode(code);
        return status == null ? defaultLabel : status.getLabel();
    }
}