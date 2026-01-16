package com.scnai.plant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 聊天服务类 - 处理AI智能问答（SSE流式响应）
 * 使用OkHttpClient集成DeepSeek API实现真实的AI对话功能
 */
@Service
public class ChatService {

    private final OkHttpClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
     * 流式问答 - 调用DeepSeek API（流式输出）
     *
     * @param message 用户消息
     * @return SSE发射器
     */
    public SseEmitter streamChat(String message) {
        SseEmitter emitter = new SseEmitter(120000L); // 2分钟超时

        executor.execute(() -> {
            try {
                // 发送开始信号
                emitter.send(SseEmitter.event()
                        .data("{\"type\":\"start\",\"timestamp\":\"" + getCurrentTimestamp() + "\"}")
                        .name("message"));

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

                // 发送请求并处理流式响应
                Response response = client.newCall(request).execute();

                if (response.code() != 200) {
                    String errorMsg = "API调用失败: " + response.code();
                    if (response.body() != null) {
                        errorMsg += " - " + response.body().string();
                    }
                    emitter.send(SseEmitter.event()
                            .data("{\"type\":\"error\",\"error\":\"" + escapeJson(errorMsg) + "\",\"timestamp\":\"" + getCurrentTimestamp() + "\"}")
                            .name("message"));
                    emitter.complete();
                    return;
                }

                // 读取流式响应
                StringBuilder fullResponse = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.body().byteStream(), StandardCharsets.UTF_8))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        // 处理SSE格式: data: {...}
                        if (line.startsWith("data: ")) {
                            String dataStr = line.substring(6).trim();

                            // 检查是否是结束标记
                            if ("[DONE]".equals(dataStr)) {
                                // 发送完成信号
                                emitter.send(SseEmitter.event()
                                        .data("{\"type\":\"complete\",\"content\":\"" + escapeJson(fullResponse.toString()) + "\",\"timestamp\":\"" + getCurrentTimestamp() + "\"}")
                                        .name("message"));
                                emitter.complete();
                                break;
                            }

                            // 解析JSON获取内容
                            try {
                                JsonNode jsonNode = objectMapper.readTree(dataStr);
                                JsonNode choices = jsonNode.path("choices");

                                if (choices.isArray() && choices.size() > 0) {
                                    JsonNode delta = choices.get(0).path("delta");

                                    if (delta.has("content")) {
                                        String contentChunk = delta.get("content").asText();
                                        fullResponse.append(contentChunk);

                                        // 发送文本块信号
                                        emitter.send(SseEmitter.event()
                                                .data("{\"type\":\"chunk\",\"content\":\"" + escapeJson(contentChunk) + "\",\"timestamp\":\"" + getCurrentTimestamp() + "\"}")
                                                .name("message"));
                                    }
                                }
                            } catch (Exception e) {
                                // 跳过无法解析的行
                                continue;
                            }
                        }
                    }
                }

            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event()
                            .data("{\"type\":\"error\",\"error\":\"" + escapeJson("生成回复时发生错误: " + e.getMessage()) + "\",\"timestamp\":\"" + getCurrentTimestamp() + "\"}")
                            .name("message"));
                } catch (IOException ex) {
                    // 忽略
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * 获取当前时间戳
     */
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(formatter);
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
