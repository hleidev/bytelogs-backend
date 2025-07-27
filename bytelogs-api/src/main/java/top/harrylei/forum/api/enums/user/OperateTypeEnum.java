package top.harrylei.forum.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.harrylei.forum.api.enums.base.CodeLabelEnum;
import top.harrylei.forum.api.enums.base.EnumCodeLabelJsonSerializer;
import top.harrylei.forum.api.enums.comment.CommentStatusEnum;
import top.harrylei.forum.api.enums.comment.ContentTypeEnum;

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
@JsonSerialize(using = EnumCodeLabelJsonSerializer.class)
public enum OperateTypeEnum implements CodeLabelEnum {

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
    // 根据枚举名称（不区分大小写）快速定位枚举实例
    private static final Map<String, OperateTypeEnum> NAME_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(e -> e.name().toUpperCase(), Function.identity()));

    /**
     * 获取操作码
     *
     * @return 操作码
     */
    @JsonValue
    @Override
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
    public int getStatusCode() {
        return switch (this) {
            case READ -> ReadStatusEnum.READ.getCode();
            case PRAISE -> PraiseStatusEnum.PRAISE.getCode();
            case COLLECTION -> CollectionStatusEnum.COLLECTION.getCode();
            case CANCEL_PRAISE -> PraiseStatusEnum.NOT_PRAISE.getCode();
            case CANCEL_COLLECTION -> CollectionStatusEnum.NOT_COLLECTION.getCode();
            case COMMENT -> CommentStatusEnum.COMMENT.getCode();
            case DELETE_COMMENT -> CommentStatusEnum.NOT_COMMENT.getCode();
            default -> 0;
        };
    }

    /**
     * 判断操作的是否是文章
     *
     * @param type 操作类型
     * @return 内容类型枚举，评论相关操作返回 COMMENT，其他返回 ARTICLE
     */
    public static ContentTypeEnum getOperateType(OperateTypeEnum type) {
        return (type == COMMENT || type == DELETE_COMMENT) ? ContentTypeEnum.COMMENT : ContentTypeEnum.ARTICLE;
    }

}
