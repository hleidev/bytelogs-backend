package top.harrylei.forum.api.model.enums.article;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文章状态枚举
 * 状态：0-草稿，1-待审核，2-已发布，3-下架，4-驳回
 */
@Getter
@AllArgsConstructor
public enum ArticleStatusEnum {

    /**
     * 草稿
     */
    DRAFT(0, "草稿"),
    /**
     * 待审核
     */
    PENDING(1, "待审核"),
    /**
     * 已发布
     */
    PUBLISHED(2, "已发布"),
    /**
     * 下架
     */
    OFFLINE(3, "下架"),
    /**
     * 驳回
     */
    REJECTED(4, "驳回");

    // 编码（唯一标识）
    private final Integer code;

    // 描述（用于展示）
    private final String label;

    // 根据编码快速定位枚举实例
    private static final Map<Integer, ArticleStatusEnum> CODE_MAP =
        Arrays.stream(values()).collect(Collectors.toMap(ArticleStatusEnum::getCode, Function.identity()));
    // 根据枚举名称（不区分大小写）快速定位枚举实例
    private static final Map<String, ArticleStatusEnum> NAME_MAP =
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
    public static ArticleStatusEnum fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }
}