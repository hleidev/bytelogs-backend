package top.harrylei.forum.api.enums.article;

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
 * 发布状态枚举
 *
 * @author harry
 */
@Getter
@AllArgsConstructor
@JsonSerialize(using = EnumCodeLabelJsonSerializer.class)
public enum PublishStatusEnum implements CodeLabelEnum {

    /**
     * 未发布
     */
    DRAFT(0, "未发布"),

    /**
     * 已发布
     */
    PUBLISHED(1, "已发布"),

    /**
     * 待审核
     */
    REVIEW(2, "待审核"),

    /**
     * 审核驳回
     */
    REJECTED(3, "审核驳回");

    // 状态编码（唯一标识）
    @EnumValue
    private final Integer code;

    // 状态描述（用于展示）
    private final String label;

    // 根据状态编码快速定位枚举实例
    private static final Map<Integer, PublishStatusEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(PublishStatusEnum::getCode, Function.identity()));

    /**
     * 获取状态码
     *
     * @return 状态码
     */
    @JsonValue
    @Override
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
    public static PublishStatusEnum fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }
}