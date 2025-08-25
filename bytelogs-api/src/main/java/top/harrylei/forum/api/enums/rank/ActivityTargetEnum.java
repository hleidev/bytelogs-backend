package top.harrylei.forum.api.enums.rank;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.harrylei.forum.api.enums.base.CodeLabelEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 活跃度目标类型枚举
 *
 * @author harry
 */
@Getter
@AllArgsConstructor
public enum ActivityTargetEnum implements CodeLabelEnum {

    /**
     * 文章
     */
    ARTICLE(1, "文章"),

    /**
     * 用户
     */
    USER(2, "用户"),

    /**
     * 评论
     */
    COMMENT(3, "评论");

    // 目标类型编码（唯一标识）
    @EnumValue
    private final Integer code;

    // 目标类型描述（用于展示）
    private final String label;

    // 根据编码快速定位枚举实例
    private static final Map<Integer, ActivityTargetEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(ActivityTargetEnum::getCode, Function.identity()));

    /**
     * 获取目标类型编码
     *
     * @return 目标类型编码
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
    public static ActivityTargetEnum fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }
}