package top.harrylei.community.api.enums.user;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.harrylei.community.api.enums.article.CollectionStatusEnum;
import top.harrylei.community.api.enums.comment.CommentStatusEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 操作类型枚举
 *
 * @author harry
 */
@Getter
@AllArgsConstructor
public enum OperateTypeEnum {

    /**
     * 空操作
     */
    EMPTY(0, ""),

    /**
     * 阅读
     */
    READ(1, "阅读"),

    /**
     * 评论
     */
    COMMENT(2, "评论"),

    /**
     * 点赞
     */
    PRAISE(3, "点赞"),

    /**
     * 收藏
     */
    COLLECTION(4, "收藏"),
    /**
     * 删除评论
     */
    DELETE_COMMENT(5, "删除评论"),

    /**
     * 取消点赞
     */
    CANCEL_PRAISE(6, "取消点赞"),

    /**
     * 取消收藏
     */
    CANCEL_COLLECTION(7, "取消收藏");


    // 操作编码（唯一标识）
    @EnumValue
    private final Integer code;

    // 操作描述（用于展示）
    private final String label;

    // 根据操作编码快速定位枚举实例
    private static final Map<Integer, OperateTypeEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(OperateTypeEnum::getCode, Function.identity()));

    /**
     * 获取操作码
     *
     * @return 操作码
     */
    @JsonValue
    public Integer getCode() {
        return code;
    }

    /**
     * 根据操作编码获取枚举对象
     *
     * @param code 操作编码
     * @return 对应的操作枚举，若无匹配则返回 null
     */
    @JsonCreator
    public static OperateTypeEnum fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }

    /**
     * 获取数据库状态码
     *
     * @return 数据库状态码
     */
    public Enum<?> getStatus() {
        return switch (this) {
            case READ -> ReadStatusEnum.READ;
            case PRAISE -> PraiseStatusEnum.PRAISE;
            case COLLECTION -> CollectionStatusEnum.COLLECTION;
            case CANCEL_PRAISE -> PraiseStatusEnum.NOT_PRAISE;
            case CANCEL_COLLECTION -> CollectionStatusEnum.NOT_COLLECTION;
            case COMMENT -> CommentStatusEnum.COMMENT;
            case DELETE_COMMENT -> CommentStatusEnum.NOT_COMMENT;
            default -> null;
        };
    }

    /**
     * 判断是否是点赞收藏相关操作
     *
     * @return 是否是点赞收藏操作
     */
    public boolean isPraiseOrCollection() {
        return this == PRAISE || this == CANCEL_PRAISE || this == COLLECTION || this == CANCEL_COLLECTION;
    }

    /**
     * 判断是否是评论可用的操作
     *
     * @return 是否是评论支持的操作
     */
    public boolean isPraise() {
        return this == PRAISE || this == CANCEL_PRAISE;
    }

}
