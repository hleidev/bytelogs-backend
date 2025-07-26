package top.harrylei.forum.api.enums;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.harrylei.forum.api.enums.base.CodeLabelEnum;
import top.harrylei.forum.api.enums.base.EnumCodeLabelJsonSerializer;
/**
 * 删除标识枚举
 *
 * @author harry
 */
@Getter
@AllArgsConstructor
@JsonSerialize(using = EnumCodeLabelJsonSerializer.class)
public enum YesOrNoEnum implements CodeLabelEnum {
    NO(0, "N"),
    YES(1,"Y");
    // 编码（唯一标识）
    @EnumValue
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
}
