package com.scnai.plant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 对话历史实体
 */
@Data
@Entity
@Table(name = "chat_history")
public class ChatHistory {
    /**
     * 消息ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    /**
     * 会话ID
     */
    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    /**
     * 角色：user/assistant
     */
    @Column(nullable = false, length = 20)
    private String role;

    /**
     * 消息内容
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * 使用的模型名称
     */
    @Column(name = "model_name", length = 50)
    private String modelName;

    /**
     * 创建时间
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
