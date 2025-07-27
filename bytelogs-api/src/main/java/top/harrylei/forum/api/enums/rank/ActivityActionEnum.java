package top.harrylei.forum.api.enums.rank;

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
 * 活跃度行为类型枚举
 *
 * @author harry
 */
@Getter
@AllArgsConstructor
@JsonSerialize(using = EnumCodeLabelJsonSerializer.class)
public enum ActivityActionEnum implements CodeLabelEnum {

    /**
     * 发文
     */
    ARTICLE(1, "发文", 10),

    /**
     * 评论
     */
    COMMENT(2, "评论", 2),

    /**
     * 点赞
     */
    PRAISE(3, "点赞", 2),

    /**
     * 收藏
     */
    COLLECT(4, "收藏", 2),

    /**
     * 阅读
     */
    READ(5, "阅读", 1),

    /**
     * 关注
     */
    FOLLOW(6, "关注", 2);

    // 行为编码（唯一标识）
    @EnumValue
    private final Integer code;

    // 行为描述（用于展示）
    private final String label;

    // 默认积分值
    private final Integer score;

    // 根据编码快速定位枚举实例
    private static final Map<Integer, ActivityActionEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(ActivityActionEnum::getCode, Function.identity()));

    /**
     * 获取行为编码
     *
     * @return 行为编码
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
    public static ActivityActionEnum fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }
}