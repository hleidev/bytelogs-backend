package top.harrylei.forum.api.enums.article;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文章来源枚举
 *
 * @author harry
 */
@Getter
@AllArgsConstructor
public enum ArticleSourceEnum {

    /**
     * 转载
     */
    REPRINT(1, "转载"),
    /**
     * 原创
     */
    ORIGINAL(2, "原创"),
    /**
     * 翻译
     */
    TRANSLATION(3, "翻译");

    // 编码（唯一标识）
    @EnumValue
    private final Integer code;

    // 描述（用于展示）
    private final String label;

    // 根据编码快速定位枚举实例
    private static final Map<Integer, ArticleSourceEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(ArticleSourceEnum::getCode, Function.identity()));
    // 根据枚举名称（不区分大小写）快速定位枚举实例
    private static final Map<String, ArticleSourceEnum> NAME_MAP =
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
    public static ArticleSourceEnum fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }
}