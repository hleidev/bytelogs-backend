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
     * @param request   请求
     * @param modelName 具体模型名称
     * @return 响应
     */
    ChatResponse chat(ChatRequest request, String modelName);

    /**
     * 获取客户端类型
     *
     * @return 类型枚举
     */
    AIClientTypeEnum getType();
}