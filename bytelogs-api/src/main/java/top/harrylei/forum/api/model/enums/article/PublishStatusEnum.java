package top.harrylei.forum.api.model.enums.article;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import top.harrylei.forum.api.model.enums.base.CodeLabelEnum;
import top.harrylei.forum.api.model.enums.base.EnumCodeLabelJsonSerializer;

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
    // 根据枚举名称（不区分大小写）快速定位枚举实例
    private static final Map<String, PublishStatusEnum> NAME_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(e -> e.name().toUpperCase(), Function.identity()));

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
    public static PublishStatusEnum fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }

    /**
     * 根据状态名称获取枚举对象（忽略大小写）
     *
     * @param name 枚举名称
     * @return 对应的状态枚举，若无匹配或为空则返回 null
     */
    public static PublishStatusEnum fromName(String name) {
        if (StringUtils.isBlank(name))
            return null;
        return NAME_MAP.get(name.toUpperCase());
    }

    /**
     * 根据状态编码获取状态描述
     *
     * @param code 状态编码
     * @return 状态描述，若无匹配则返回 null
     */
    public static String getLabelByCode(Integer code) {
        PublishStatusEnum status = fromCode(code);
        return status == null ? null : status.getLabel();
    }

    /**
     * 根据状态编码获取状态标签，若未找到则返回默认值
     *
     * @param code         状态编码
     * @param defaultLabel 默认值
     * @return 状态描述，若无匹配则返回默认值
     */
    public static String getLabelByCode(Integer code, String defaultLabel) {
        PublishStatusEnum status = fromCode(code);
        return status == null ? defaultLabel : status.getLabel();
    }

    /**
     * 根据状态名称获取状态编码
     *
     * @param name 状态名称
     * @return 状态编码，若无匹配则返回 null
     */
    public static Integer getCodeByName(String name) {
        PublishStatusEnum status = fromName(name);
        return status == null ? null : status.getCode();
    }

    /**
     * 根据状态编码获取状态名称（枚举名）
     *
     * @param code 状态编码
     * @return 状态名称，若无匹配则返回 DRAFT
     */
    public static String getNameByCode(Integer code) {
        PublishStatusEnum status = fromCode(code);
        return status == null ? DRAFT.name() : status.name();
    }

    /**
     * 根据状态编码获取状态名称（枚举名）
     *
     * @param code        状态编码
     * @param defaultName 默认状态名
     * @return 状态名称，若无匹配则返回默认值
     */
    public static String getNameByCode(Integer code, String defaultName) {
        PublishStatusEnum status = fromCode(code);
        return status == null ? defaultName : status.name();
    }
}