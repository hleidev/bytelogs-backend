package top.harrylei.community.api.enums.websocket;

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
 * WebSocket消息类型枚举
 *
 * @author harry
 */
@Getter
@AllArgsConstructor
public enum WebSocketMessageType {

    /**
     * AI聊天流式响应
     */
    AI_STREAM(1, "AI流式响应"),

    /**
     * 通知消息
     */
    NOTIFICATION(2, "通知消息"),

    /**
     * 系统消息
     */
    SYSTEM(3, "系统消息"),

    /**
     * 连接状态
     */
    CONNECTION(4, "连接状态"),

    /**
     * 心跳消息
     */
    HEARTBEAT(5, "心跳消息"),

    /**
     * 错误消息
     */
    ERROR(6, "错误消息");

    // 编码（唯一标识）
    @EnumValue
    private final Integer code;

    // 描述（用于展示）
    private final String label;

    // 根据编码快速定位枚举实例
    private static final Map<Integer, WebSocketMessageType> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(WebSocketMessageType::getCode, Function.identity()));

    /**
     * 获取编码
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
    public static WebSocketMessageType fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }
}