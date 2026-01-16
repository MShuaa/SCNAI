package com.scnai.plant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 识别记录实体
 */
@Data
@Entity
@Table(name = "recognition_records")
public class RecognitionRecord {
    /**
     * 记录ID
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
     * 植物ID
     */
    @Column(name = "plant_id")
    private Integer plantId;

    /**
     * 植物名称
     */
    @Column(name = "plant_name", length = 100)
    private String plantName = "丝瓜";

    /**
     * 图像URL
     */
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    /**
     * 缩略图URL
     */
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    /**
     * 病害类型代码
     */
    @Column(name = "disease_type", nullable = false, length = 50)
    private String diseaseType;

    /**
     * 病害名称
     */
    @Column(name = "disease_type_name", nullable = false, length = 50)
    private String diseaseTypeName;

    /**
     * 置信度（0-1）
     */
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal confidence;

    /**
     * 严重程度
     */
    @Column(nullable = false, length = 20)
    private String severity;

    /**
     * 区域
     */
    @Column(length = 50)
    private String area;

    /**
     * 纬度
     */
    @Column(name = "location_lat", precision = 10, scale = 7)
    private BigDecimal locationLat;

    /**
     * 经度
     */
    @Column(name = "location_lng", precision = 10, scale = 7)
    private BigDecimal locationLng;

    /**
     * 处理状态
     */
    @Column(length = 20)
    private String status = "未处理";

    /**
     * 症状描述
     */
    @Column(columnDefinition = "TEXT")
    private String symptoms;

    /**
     * 处理方案
     */
    @Column(name = "treatment_plan", columnDefinition = "TEXT")
    private String treatmentPlan;

    /**
     * 识别时间
     */
    @Column(name = "identify_time")
    private LocalDateTime identifyTime;

    /**
     * 创建时间
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (identifyTime == null) {
            identifyTime = LocalDateTime.now();
        }
    }
}
