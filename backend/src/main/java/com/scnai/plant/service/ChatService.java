package com.scnai.plant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 聊天服务类 - 处理AI智能问答（SSE流式响应）
 * 使用OkHttpClient集成DeepSeek API实现真实的AI对话功能
 */
@Service
public class ChatService {

    private final OkHttpClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // DeepSeek API配置（从application.properties读取）
    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.model}")
    private String model;

    @Value("${deepseek.max.tokens}")
    private int maxTokens;

    @Value("${deepseek.temperature}")
    private double temperature;

    /**
     * 构造函数 - 初始化OkHttpClient用于调用DeepSeek API
     */
    public ChatService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 流式问答 - 调用DeepSeek API
     *
     * @param message 用户消息
     * @return SSE发射器
     */
    public SseEmitter streamChat(String message) {
        SseEmitter emitter = new SseEmitter(120000L); // 2分钟超时

        try {
            // 检查API密钥是否配置
            if (apiKey == null || apiKey.equals("YOUR_DEEPSEEK_API_KEY_HERE")) {
                emitter.send(SseEmitter.event()
                        .data("{\"type\":\"error\",\"error\":\"DeepSeek API密钥未配置，请在application.properties中设置deepseek.api.key\"}")
                        .name("message"));
                emitter.complete();
                return emitter;
            }

            // 构建DeepSeek API请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", "你是SCNAI植物病虫害智能助手，专注于植物病虫害识别、防治建议和栽培管理指导。请用专业但易懂的语言回答问题。"),
                    Map.of("role", "user", "content", message)
            ));
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", temperature);
            requestBody.put("stream", true); // 启用流式响应

            // 构建HTTP请求
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(jsonBody, MediaType.get("application/json")))
                    .build();

            // 使用EventSource处理SSE流式响应
            EventSource.Factory factory = EventSources.createFactory(client);
            StringBuilder fullContent = new StringBuilder();

            factory.newEventSource(request, new EventSourceListener() {
                @Override
                public void onOpen(EventSource eventSource, Response response) {
                    // 连接已建立
                }

                @Override
                public void onEvent(EventSource eventSource, String id, String type, String data) {
                    try {
                        // DeepSeek返回的data格式: {...}
                        if ("[DONE]".equals(data)) {
                            // 发送完成事件
                            emitter.send(SseEmitter.event()
                                    .data("{\"type\":\"complete\",\"fullContent\":\"" + escapeJson(fullContent.toString()) + "\"}")
                                    .name("message"));
                            emitter.complete();
                            eventSource.cancel();
                            return;
                        }

                        // 解析JSON获取内容
                        JsonNode jsonNode = objectMapper.readTree(data);
                        JsonNode choices = jsonNode.path("choices");

                        if (choices.isArray() && choices.size() > 0) {
                            JsonNode delta = choices.get(0).path("delta");

                            if (delta.has("content")) {
                                String content = delta.get("content").asText();
                                fullContent.append(content);

                                // 发送内容块
                                emitter.send(SseEmitter.event()
                                        .data("{\"type\":\"chunk\",\"content\":\"" + escapeJson(content) + "\"}")
                                        .name("message"));
                            }
                        }
                    } catch (Exception e) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .data("{\"type\":\"error\",\"error\":\"处理响应时出错: " + escapeJson(e.getMessage()) + "\"}")
                                    .name("message"));
                        } catch (IOException ex) {
                            // 忽略
                        }
                        emitter.completeWithError(e);
                        eventSource.cancel();
                    }
                }

                @Override
                public void onClosed(EventSource eventSource) {
                    // 连接关闭
                    if (fullContent.length() > 0) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .data("{\"type\":\"complete\",\"fullContent\":\"" + escapeJson(fullContent.toString()) + "\"}")
                                    .name("message"));
                        } catch (IOException e) {
                            // 忽略
                        }
                    }
                    emitter.complete();
                }

                @Override
                public void onFailure(EventSource eventSource, Throwable t, Response response) {
                    // 调用失败
                    try {
                        String errorMsg = "调用DeepSeek API失败";
                        if (response != null) {
                            errorMsg += ": HTTP " + response.code();
                            if (response.body() != null) {
                                errorMsg += " - " + response.body().string();
                            }
                        } else if (t != null) {
                            errorMsg += ": " + t.getMessage();
                        }

                        emitter.send(SseEmitter.event()
                                .data("{\"type\":\"error\",\"error\":\"" + escapeJson(errorMsg) + "\"}")
                                .name("message"));
                    } catch (IOException ex) {
                        // 忽略
                    }
                    emitter.completeWithError(t);
                    eventSource.cancel();
                }
            });

        } catch (Exception e) {
            try {
                emitter.send(SseEmitter.event()
                        .data("{\"type\":\"error\",\"error\":\"" + escapeJson(e.getMessage()) + "\"}")
                        .name("message"));
            } catch (IOException ex) {
                // 忽略
            }
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * JSON字符串转义
     */
    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
