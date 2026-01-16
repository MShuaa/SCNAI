package com.scnai.plant.dto;

import lombok.Data;
import java.util.List;

/**
 * 聊天请求DTO
 */
@Data
public class ChatRequest {
    /**
     * 用户消息
     */
    private String message;

    /**
     * 对话历史(可选)
     */
    private List<ChatMessage> history;

    /**
     * 会话ID(可选)
     */
    private String sessionId;

    @Data
    public static class ChatMessage {
        private String role; // user/assistant
        private String content;
    }
}
