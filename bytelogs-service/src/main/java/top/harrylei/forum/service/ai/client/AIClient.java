package top.harrylei.forum.service.ai.client;

import top.harrylei.forum.api.enums.ai.AIClientTypeEnum;

/**
 * AI客户端统一接口
 *
 * @author harry
 */
public interface AIClient {

    /**
     * 发送聊天请求
     *
     * @param request 请求
     * @return 响应
     */
    ChatResponse chat(ChatRequest request);

    /**
     * 获取客户端类型
     *
     * @return 类型枚举
     */
    AIClientTypeEnum getType();
}