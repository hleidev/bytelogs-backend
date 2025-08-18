package top.harrylei.forum.service.ai.client;

import org.springframework.stereotype.Component;
import top.harrylei.forum.api.enums.ai.AIClientTypeEnum;
import top.harrylei.forum.core.config.AIConfig;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AI客户端工厂
 *
 * @author harry
 */
@Component
public class AIClientFactory {

    private final AIConfig aiConfig;
    private final Map<AIClientTypeEnum, AIClient> clients;

    public AIClientFactory(AIConfig aiConfig, List<AIClient> clientList) {
        this.aiConfig = aiConfig;
        this.clients = clientList.stream().collect(Collectors.toMap(AIClient::getType, Function.identity()));
    }

    /**
     * 获取默认客户端
     */
    public AIClient getDefaultClient() {
        AIClientTypeEnum defaultType = AIClientTypeEnum.fromConfigKey(aiConfig.getDefaultClient());
        if (defaultType == null) {
            throw new IllegalArgumentException("无效的默认客户端配置: " + aiConfig.getDefaultClient());
        }
        return getClient(defaultType);
    }

    /**
     * 获取指定类型的客户端
     */
    public AIClient getClient(AIClientTypeEnum clientType) {
        AIClient client = clients.getOrDefault(clientType, null);
        if (client == null) {
            throw new IllegalArgumentException("不支持的AI客户端类型: " + clientType.getLabel());
        }
        return client;
    }

    /**
     * 获取所有支持的客户端类型
     */
    public AIClientTypeEnum[] getSupportedClients() {
        return clients.keySet().toArray(new AIClientTypeEnum[0]);
    }
}