package com.scnai.plant.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 识别响应DTO
 */
@Data
public class RecognitionResponse {
    /**
     * 记录ID
     */
    private Integer id;

    /**
     * 病害类型代码
     */
    private String diseaseType;

    /**
     * 病害类型名称
     */
    private String diseaseTypeName;

    /**
     * 置信度
     */
    private BigDecimal confidence;

    /**
     * 严重程度
     */
    private String severity;

    /**
     * 识别时间
     */
    private String identifyTime;

    /**
     * 图像URL
     */
    private String imageUrl;

    /**
     * 缩略图URL
     */
    private String thumbnailUrl;

    /**
     * 症状描述
     */
    private String symptoms;

    /**
     * 处理方案
     */
    private String treatmentPlan;

    /**
     * 原始预测结果（所有类别的置信度）
     */
    private Map<String, BigDecimal> rawPredictions;
}
