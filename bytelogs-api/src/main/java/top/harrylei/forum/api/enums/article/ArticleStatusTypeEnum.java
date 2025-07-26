package top.harrylei.forum.api.enums.article;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文章属性类型枚举
 *
 * @author harry
 * @since 2025/6/22
 */
@Getter
@AllArgsConstructor
public enum ArticleStatusTypeEnum {

    /**
     * 置顶
     */
    TOPPING(1, "置顶"),

    /**
     * 加精
     */
    CREAM(2, "加精"),

    /**
     * 官方
     */
    OFFICIAL(3, "官方");

    // 编码（唯一标识）
    private final Integer code;

    // 描述（用于展示）
    private final String label;

    // 根据编码快速定位枚举实例
    private static final Map<Integer, ArticleStatusTypeEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(ArticleStatusTypeEnum::getCode, Function.identity()));

    /**
     * 获取编码（JSON序列化时使用）
     *
     * @return 编码
     */
    @JsonValue
    public Integer getCode() {
        return code;
    }

    /**
     * 根据编码获取枚举对象（JSON反序列化时使用）
     *
     * @param code 编码
     * @return 对应的枚举，若无匹配则返回 null
     */
    @JsonCreator
    public static ArticleStatusTypeEnum fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }
}