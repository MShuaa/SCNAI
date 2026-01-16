package com.scnai.plant.controller;

import com.scnai.plant.dto.ChatRequest;
import com.scnai.plant.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 聊天控制器 - 处理AI智能问答（SSE流式响应）
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * 流式问答接口
     *
     * @param request 聊天请求
     * @return SSE流
     */
    @PostMapping("/stream")
    public SseEmitter streamChat(@RequestBody ChatRequest request) {
        return chatService.streamChat(request.getMessage());
    }
}
