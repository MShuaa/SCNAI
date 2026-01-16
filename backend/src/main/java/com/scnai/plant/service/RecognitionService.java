package com.scnai.plant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scnai.plant.dto.RecognitionResponse;
import com.scnai.plant.entity.RecognitionRecord;
import com.scnai.plant.repository.RecognitionRecordRepository;
import okhttp3.*;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 识别服务类 - 处理图像识别逻辑,调用Python AI服务
 */
@Service
public class RecognitionService {

    @Autowired
    private RecognitionRecordRepository recordRepository;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${ai.service.url:http://localhost:5001}")
    private String aiServiceUrl;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RecognitionService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    // 病害类型映射 (基于webwork.py的class_names)
    private static final Map<String, String> DISEASE_MAP = new HashMap<>();
    static {
        DISEASE_MAP.put("tanju", "炭疽病");
        DISEASE_MAP.put("qingchong", "蚜虫");
        DISEASE_MAP.put("shuangmei", "霜霉病");
        DISEASE_MAP.put("jiankang", "健康");
        DISEASE_MAP.put("banqianying", "斑潜蝇");
        DISEASE_MAP.put("zhenfeng", "蓟马");
    }

    // 症状描述映射
    private static final Map<String, String> SYMPTOMS_MAP = new HashMap<>();
    static {
        SYMPTOMS_MAP.put("tanju", "叶片和果实出现圆形或不规则褐色病斑，中央灰白色，边缘明显，严重时病斑连片，叶片枯死");
        SYMPTOMS_MAP.put("qingchong", "叶片背面有绿色或黑色小虫聚集，叶片卷曲变形，分泌蜜露，引发煤污病");
        SYMPTOMS_MAP.put("shuangmei", "叶片背面出现灰白色霉层，正面出现黄褐色病斑，病斑逐渐扩大连片");
        SYMPTOMS_MAP.put("jiankang", "植株生长健康，叶片颜色正常，无病虫害症状");
        SYMPTOMS_MAP.put("banqianying", "叶片上出现弯曲的白色潜道，潜道内有黑色虫粪，严重时叶片枯黄");
        SYMPTOMS_MAP.put("zhenfeng", "叶片表面出现银白色斑点，叶片边缘卷曲，严重时叶片干枯脱落");
    }

    // 处理方案映射
    private static final Map<String, String> TREATMENT_MAP = new HashMap<>();
    static {
        TREATMENT_MAP.put("tanju", "建议使用咪鲜胺、炭疽福美或甲基托布津等药剂防治，及时清除病残体，避免过度密植");
        TREATMENT_MAP.put("qingchong", "建议使用吡虫啉、啶虫脒或噻虫嗪等杀虫剂防治，也可用黄板诱杀，保护瓢虫等天敌");
        TREATMENT_MAP.put("shuangmei", "建议使用烯酰吗啉、霜脲锰锌或代森锰锌等药剂防治，加强通风，降低田间湿度");
        TREATMENT_MAP.put("jiankang", "继续保持良好的栽培管理，定期巡查，预防病虫害发生");
        TREATMENT_MAP.put("banqianying", "建议使用阿维菌素、灭蝇胺或溴氰菊酯等杀虫剂防治，及时摘除被害叶片");
        TREATMENT_MAP.put("zhenfeng", "建议使用吡虫啉、啶虫脒或乙基多杀菌素等杀虫剂防治，注意喷施叶片背面");
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
        Path filePath = Paths.get(uploadPath, fileName);

        // 3. 调用Python AI服务进行识别
        Map<String, BigDecimal> predictions = callAIService(filePath.toFile());

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
        record.setDiseaseTypeName(diseaseType);
        record.setConfidence(confidence);
        record.setSeverity(severity);
        record.setArea(area);
        record.setSymptoms(SYMPTOMS_MAP.getOrDefault(diseaseType, "暂无症状描述"));
        record.setTreatmentPlan(TREATMENT_MAP.getOrDefault(diseaseType, "请咨询专业人员"));
        record.setIdentifyTime(LocalDateTime.now());

        record = recordRepository.save(record);

        // 7. 构造响应
        return buildResponse(record, predictions);
    }

    /**
     * 调用Python AI服务进行识别
     */
    private Map<String, BigDecimal> callAIService(File imageFile) throws IOException {
        // 构建multipart请求
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", imageFile.getName(),
                        RequestBody.create(imageFile, MediaType.parse("image/*")))
                .build();

        Request request = new Request.Builder()
                .url(aiServiceUrl + "/predict")
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("AI服务调用失败: HTTP " + response.code());
            }

            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            // 解析AI服务返回的结果
            Map<String, BigDecimal> predictions = new HashMap<>();

            if (jsonNode.has("all_predictions")) {
                JsonNode allPredictions = jsonNode.get("all_predictions");
                for (JsonNode pred : allPredictions) {
                    String className = pred.get("class").asText();
                    double confidence = pred.get("confidence").asDouble();
                    predictions.put(className, BigDecimal.valueOf(confidence).setScale(4, BigDecimal.ROUND_HALF_UP));
                }
            } else if (jsonNode.has("label") && jsonNode.has("confidence")) {
                // 兼容简单格式
                String label = jsonNode.get("label").asText();
                double confidence = jsonNode.get("confidence").asDouble();
                predictions.put(label, BigDecimal.valueOf(confidence).setScale(4, BigDecimal.ROUND_HALF_UP));
            } else {
                throw new IOException("AI服务返回格式错误");
            }

            return predictions;
        } catch (Exception e) {
            throw new IOException("调用AI服务失败: " + e.getMessage(), e);
        }
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

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("文件大小不能超过10MB");
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
     * 获取置信度最高的预测结果
     */
    private String getTopPrediction(Map<String, BigDecimal> predictions) {
        return predictions.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("jiankang");
    }

    /**
     * 判断严重程度
     */
    private String determineSeverity(BigDecimal confidence, String diseaseType) {
        if ("健康".equals(diseaseType)) {
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

