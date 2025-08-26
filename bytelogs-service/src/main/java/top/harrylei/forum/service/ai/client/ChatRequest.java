package top.harrylei.forum.service.ai.client;

import lombok.Data;
import top.harrylei.forum.api.enums.ai.AIMessageRoleEnum;

import java.util.List;

/**
 * 统一聊天请求
 *
 * @author harry
 */
@Data
public class ChatRequest {

    /**
     * 消息列表
     */
    private List<Message> messages;

    @Data
    public static class Message {
        private AIMessageRoleEnum role;
        private String content;

        public Message(AIMessageRoleEnum role, String content) {
            this.role = role;
            this.content = content;
        }

        public static Message user(String content) {
            return new Message(AIMessageRoleEnum.USER, content);
        }

        public static Message assistant(String content) {
            return new Message(AIMessageRoleEnum.ASSISTANT, content);
        }
    }
}