package top.harrylei.forum.api.model.enums.base;

/**
 * 通用 code-label 枚举接口。
 */
public interface CodeLabelEnum {

    /**
     * 获取业务编码
     *
     * @return 枚举对应的唯一 code
     */
    Integer getCode();

    /**
     * 获取展示用标签
     *
     * @return 枚举对应的显示 label
     */
    String getLabel();
}