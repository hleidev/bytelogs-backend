package top.harrylei.forum.api.model.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 删除标识枚举
 *
 * @author louzai
 * @since 2022/7/19
 */
@Getter
@AllArgsConstructor
public enum YesOrNoEnum {

    NO(0, "N"),
    YES(1,"Y");

    // 编码（唯一标识）
    private final Integer code;

    // 描述（用于展示）
    private final String label;

    // 根据编码快速定位枚举实例
    private static final Map<Integer, YesOrNoEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(YesOrNoEnum::getCode, Function.identity()));
    // 根据枚举名称（不区分大小写）快速定位枚举实例
    private static final Map<String, YesOrNoEnum> NAME_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(e -> e.name().toUpperCase(), Function.identity()));

    /**
     * 获取码
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
    public static YesOrNoEnum fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }

    /**
     * 根据名称获取枚举对象（忽略大小写）
     *
     * @param name 枚举名称
     * @return 对应的枚举，若无匹配或为空则返回 null
     */
    public static YesOrNoEnum fromName(String name) {
        if (StringUtils.isBlank(name))
            return null;
        return NAME_MAP.get(name.toUpperCase());
    }

    /**
     * 根据编码获取描述
     *
     * @param code 编码
     * @return 标签描述，若无匹配则返回 null
     */
    public static String getLabelByCode(Integer code) {
        YesOrNoEnum status = fromCode(code);
        return status == null ? null : status.getLabel();
    }

    /**
     * 根据编码获取标签，若未找到则返回默认值
     *
     * @param code 编码
     * @param defaultLabel 默认值
     * @return 标签描述，若无匹配则返回默认值
     */
    public static String getLabelByCode(Integer code, String defaultLabel) {
        YesOrNoEnum status = fromCode(code);
        return status == null ? defaultLabel : status.getLabel();
    }

    /**
     * 根据名称获取编码
     *
     * @param name 名称
     * @return 编码，若无匹配则返回 null
     */
    public static Integer getCodeByName(String name) {
        YesOrNoEnum status = fromName(name);
        return status == null ? null : status.getCode();
    }

    /**
     * 根据编码获取名称（枚举名）
     *
     * @param code 编码
     * @return 名称，若无匹配则返回 null
     */
    public static String getNameByCode(Integer code) {
        YesOrNoEnum status = fromCode(code);
        return status == null ? null : status.name();
    }

    /**
     * 根据编码获取名称（枚举名）
     *
     * @param code 编码
     * @param defaultName 默认名
     * @return 名称，若无匹配则返回默认值
     */
    public static String getNameByCode(Integer code, String defaultName) {
        YesOrNoEnum status = fromCode(code);
        return status == null ? defaultName : status.name();
    }
}
