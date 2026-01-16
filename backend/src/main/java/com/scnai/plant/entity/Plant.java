package com.scnai.plant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 植物信息实体
 */
@Data
@Entity
@Table(name = "plants")
public class Plant {
    /**
     * 植物ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 植物名称
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * 学名
     */
    @Column(name = "scientific_name", length = 150)
    private String scientificName;

    /**
     * 科
     */
    @Column(length = 100)
    private String family;

    /**
     * 属
     */
    @Column(length = 100)
    private String genus;

    /**
     * 描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 生长习性
     */
    @Column(name = "growth_habit", columnDefinition = "TEXT")
    private String growthHabit;

    /**
     * 种植方法
     */
    @Column(name = "planting_method", columnDefinition = "TEXT")
    private String plantingMethod;

    /**
     * 常见病害（JSON格式）
     */
    @Column(name = "common_diseases", columnDefinition = "TEXT")
    private String commonDiseases;

    /**
     * 预防措施
     */
    @Column(name = "prevention_tips", columnDefinition = "TEXT")
    private String preventionTips;

    /**
     * 图片URL
     */
    @Column(name = "image_url")
    private String imageUrl;

    /**
     * 创建时间
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
