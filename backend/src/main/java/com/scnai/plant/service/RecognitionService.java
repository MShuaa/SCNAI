package com.scnai.plant.service;

import com.scnai.plant.dto.RecognitionResponse;
import com.scnai.plant.entity.RecognitionRecord;
import com.scnai.plant.repository.RecognitionRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * 识别服务类 - 处理图像识别逻辑
 */
@Service
public class RecognitionService {

    @Autowired
    private RecognitionRecordRepository recordRepository;

    @Value("${file.upload.path}")
    private String uploadPath;

    // 病害类型映射
    private static final Map<String, String> DISEASE_MAP = new HashMap<>();
    static {
        DISEASE_MAP.put("powdery", "白粉病");
        DISEASE_MAP.put("downy", "霜霉病");
        DISEASE_MAP.put("anthracnose", "炭疽病");
        DISEASE_MAP.put("virus", "病毒病");
        DISEASE_MAP.put("aphid", "蚜虫");
        DISEASE_MAP.put("healthy", "健康");
    }

    // 症状描述映射
    private static final Map<String, String> SYMPTOMS_MAP = new HashMap<>();
    static {
        SYMPTOMS_MAP.put("powdery", "叶片表面出现白色粉状物，部分叶片开始黄化，严重时叶片枯萎脱落");
        SYMPTOMS_MAP.put("downy", "叶片背面出现灰白色霉层，正面出现黄褐色病斑，逐渐扩大连片");
        SYMPTOMS_MAP.put("anthracnose", "叶片和果实出现圆形或不规则褐色病斑，中央灰白色，边缘明显");
        SYMPTOMS_MAP.put("virus", "叶片出现黄绿相间的花叶症状，植株矮化，生长缓慢");
        SYMPTOMS_MAP.put("aphid", "叶片背面有绿色或黑色小虫聚集，叶片卷曲变形，分泌蜜露");
        SYMPTOMS_MAP.put("healthy", "植株生长健康，叶片颜色正常，无病虫害症状");
    }

    // 处理方案映射
    private static final Map<String, String> TREATMENT_MAP = new HashMap<>();
    static {
        TREATMENT_MAP.put("powdery", "建议使用硫磺粉、三唑酮或苯醚甲环唑等杀菌剂进行防治，间隔7-10天喷施一次，连续2-3次");
        TREATMENT_MAP.put("downy", "建议使用烯酰吗啉、霜脲锰锌或代森锰锌等药剂防治，加强通风，降低湿度");
        TREATMENT_MAP.put("anthracnose", "建议使用咪鲜胺、炭疽福美或甲基托布津等药剂防治，及时清除病残体");
        TREATMENT_MAP.put("virus", "该病害无特效药，建议及时拔除病株，防治蚜虫等传播媒介，选用抗病品种");
        TREATMENT_MAP.put("aphid", "建议使用吡虫啉、啶虫脒或噻虫嗪等杀虫剂防治，也可用黄板诱杀");
        TREATMENT_MAP.put("healthy", "继续保持良好的栽培管理，定期巡查，预防病虫害发生");
    }

    /**
     * 识别图像
     *
     * @param file 上传的图像文件
     * @param userId 用户ID
     * @param area 区域（可选）
     * @return 识别结果
     */
    public RecognitionResponse recognize(MultipartFile file, Integer userId, String area) throws IOException {
        // 1. 验证文件
        validateFile(file);

        // 2. 保存文件
        String fileName = saveFile(file);
        String imageUrl = "/uploads/recognition/" + fileName;

        // 3. AI识别（模拟实现 - 实际项目中应该调用AI模型）
        Map<String, BigDecimal> predictions = mockAIRecognition();

        // 4. 获取置信度最高的病害类型
        String diseaseType = getTopPrediction(predictions);
        BigDecimal confidence = predictions.get(diseaseType);

        // 5. 判断严重程度
        String severity = determineSeverity(confidence, diseaseType);

        // 6. 保存识别记录
        RecognitionRecord record = new RecognitionRecord();
        record.setUserId(userId);
        record.setPlantName("丝瓜");
        record.setImageUrl(imageUrl);
        record.setDiseaseType(diseaseType);
        record.setDiseaseTypeName(DISEASE_MAP.get(diseaseType));
        record.setConfidence(confidence);
        record.setSeverity(severity);
        record.setArea(area);
        record.setSymptoms(SYMPTOMS_MAP.get(diseaseType));
        record.setTreatmentPlan(TREATMENT_MAP.get(diseaseType));
        record.setIdentifyTime(LocalDateTime.now());

        record = recordRepository.save(record);

        // 7. 构造响应
        return buildResponse(record, predictions);
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new RuntimeException("文件格式不支持，仅支持JPG和PNG格式");
        }

        if (file.getSize() > 3 * 1024 * 1024) {
            throw new RuntimeException("文件大小不能超过3MB");
        }
    }

    /**
     * 保存文件
     */
    private String saveFile(MultipartFile file) throws IOException {
        // 创建上传目录
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + extension;

        // 保存文件
        Path filePath = Paths.get(uploadPath, fileName);
        Files.write(filePath, file.getBytes());

        return fileName;
    }

    /**
     * 模拟AI识别（实际项目中应该调用真实的AI模型）
     */
    private Map<String, BigDecimal> mockAIRecognition() {
        Random random = new Random();
        Map<String, BigDecimal> predictions = new HashMap<>();

        // 随机生成各类别置信度（模拟AI输出）
        String[] diseases = {"powdery", "downy", "anthracnose", "virus", "aphid", "healthy"};
        int topIndex = random.nextInt(diseases.length);

        double sum = 0.0;
        for (int i = 0; i < diseases.length; i++) {
            double confidence;
            if (i == topIndex) {
                // 最高置信度：0.7-0.95
                confidence = 0.7 + random.nextDouble() * 0.25;
            } else {
                // 其他置信度：0.01-0.1
                confidence = 0.01 + random.nextDouble() * 0.09;
            }
            predictions.put(diseases[i], BigDecimal.valueOf(confidence).setScale(4, BigDecimal.ROUND_HALF_UP));
            sum += confidence;
        }

        // 归一化（确保总和为1）
        for (String disease : diseases) {
            BigDecimal normalized = predictions.get(disease).divide(BigDecimal.valueOf(sum), 4, BigDecimal.ROUND_HALF_UP);
            predictions.put(disease, normalized);
        }

        return predictions;
    }

    /**
     * 获取置信度最高的预测结果
     */
    private String getTopPrediction(Map<String, BigDecimal> predictions) {
        return predictions.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("healthy");
    }

    /**
     * 判断严重程度
     */
    private String determineSeverity(BigDecimal confidence, String diseaseType) {
        if ("healthy".equals(diseaseType)) {
            return "健康";
        }

        double conf = confidence.doubleValue();
        if (conf > 0.8) {
            return "重度";
        } else if (conf > 0.5) {
            return "中度";
        } else {
            return "轻度";
        }
    }

    /**
     * 构造响应
     */
    private RecognitionResponse buildResponse(RecognitionRecord record, Map<String, BigDecimal> predictions) {
        RecognitionResponse response = new RecognitionResponse();
        response.setId(record.getId());
        response.setDiseaseType(record.getDiseaseType());
        response.setDiseaseTypeName(record.getDiseaseTypeName());
        response.setConfidence(record.getConfidence());
        response.setSeverity(record.getSeverity());
        response.setIdentifyTime(record.getIdentifyTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        response.setImageUrl(record.getImageUrl());
        response.setSymptoms(record.getSymptoms());
        response.setTreatmentPlan(record.getTreatmentPlan());
        response.setRawPredictions(predictions);
        return response;
    }
}
