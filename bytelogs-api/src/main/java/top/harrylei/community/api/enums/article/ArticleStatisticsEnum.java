package top.harrylei.community.api.enums.article;

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
 * 文章统计操作类型枚举
 *
 * @author harry
 */
@Getter
@AllArgsConstructor
public enum ArticleStatisticsEnum {

    /**
     * 增加阅读量
     */
    INCREMENT_READ(1, "增加阅读量"),

    /**
     * 增加点赞量
     */
    INCREMENT_PRAISE(2, "增加点赞量"),

    /**
     * 减少点赞量
     */
    DECREMENT_PRAISE(3, "减少点赞量"),

    /**
     * 增加收藏量
     */
    INCREMENT_COLLECT(4, "增加收藏量"),

    /**
     * 减少收藏量
     */
    DECREMENT_COLLECT(5, "减少收藏量"),

    /**
     * 增加评论量
     */
    INCREMENT_COMMENT(6, "增加评论量"),

    /**
     * 减少评论量
     */
    DECREMENT_COMMENT(7, "减少评论量");

    // 操作编码（唯一标识）
    @EnumValue
    private final Integer code;

    // 操作描述（用于展示）
    private final String label;

    // 根据编码快速定位枚举实例
    private static final Map<Integer, ArticleStatisticsEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(ArticleStatisticsEnum::getCode, Function.identity()));

    /**
     * 获取操作编码
     *
     * @return 操作编码
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
    public static ArticleStatisticsEnum fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }
}