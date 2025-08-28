package top.harrylei.community.api.enums.rank;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.harrylei.community.api.enums.base.CodeLabelEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 排行榜类型枚举
 *
 * @author harry
 */
@Getter
@AllArgsConstructor
public enum ActivityRankTypeEnum implements CodeLabelEnum {

    /**
     * 总榜
     */
    TOTAL(1, "总榜"),

    /**
     * 月榜
     */
    MONTHLY(2, "月榜"),

    /**
     * 日榜
     */
    DAILY(3, "日榜");

    // 类型编码（唯一标识）
    @EnumValue
    private final Integer code;

    // 类型描述（用于展示）
    private final String label;

    // 根据编码快速定位枚举实例
    private static final Map<Integer, ActivityRankTypeEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(ActivityRankTypeEnum::getCode, Function.identity()));

    /**
     * 获取类型编码
     *
     * @return 类型编码
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
    public static ActivityRankTypeEnum fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }
}