package top.harrylei.forum.api.model.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatusEnum {

    DISABLED(0, "禁用"),
    ENABLE(1, "启动");

    private final int code;
    private final String desc;
}

