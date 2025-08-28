package top.harrylei.community.core.common.converter;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import top.harrylei.community.api.enums.YesOrNoEnum;
import top.harrylei.community.api.enums.article.PublishStatusEnum;
import top.harrylei.community.api.enums.article.TagTypeEnum;

/**
 * 通用枚举转换器
 *
 * @author harry
 */
@Component
public class EnumConverter {

    @Named("PublishStatusEnumToCode")
    public Integer publishStatusEnumToCode(PublishStatusEnum publishStatusEnum) {
        return publishStatusEnum == null ? null : publishStatusEnum.getCode();
    }

    @Named("PublishStatusEnumToLabel")
    public String publishStatusEnumToLabel(PublishStatusEnum publishStatusEnum) {
        return publishStatusEnum == null ? null : publishStatusEnum.getLabel();
    }

    @Named("CodeToPublishStatusEnum")
    public PublishStatusEnum codeToPublishStatusEnum(Integer code) {
        return code == null ? null : PublishStatusEnum.fromCode(code);
    }

    @Named("YesOrNoEnumToCode")
    public Integer yesOrNoEnumToCode(YesOrNoEnum yesOrNoEnum) {
        return yesOrNoEnum == null ? null : yesOrNoEnum.getCode();
    }

    @Named("YesOrNoEnumToLabel")
    public String yesOrNoEnumToLabel(YesOrNoEnum yesOrNoEnum) {
        return yesOrNoEnum == null ? null : yesOrNoEnum.getLabel();
    }

    @Named("CodeToYesOrNoEnum")
    public YesOrNoEnum codeToYesOrNoEnum(Integer code) {
        return code == null ? null : YesOrNoEnum.fromCode(code);
    }

    @Named("TagTypeEnumToCode")
    public Integer tagTypeEnumToCode(TagTypeEnum tagTypeEnum) {
        return tagTypeEnum == null ? null : tagTypeEnum.getCode();
    }

    @Named("CodeToTagTypeEnum")
    public TagTypeEnum codeToTagTypeEnum(Integer code) {
        return code == null ? null : TagTypeEnum.fromCode(code);
    }

}