package com.scnai.plant.controller;

import com.scnai.plant.common.ApiResponse;
import com.scnai.plant.dto.RecognitionResponse;
import com.scnai.plant.service.RecognitionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 识别控制器 - 处理图像识别请求
 */
@RestController
@RequestMapping("/recognition")
public class RecognitionController {

    @Autowired
    private RecognitionService recognitionService;

    /**
     * 识别图像
     *
     * @param file 上传的图像文件
     * @param area 区域（可选）
     * @param request HTTP请求（从属性中获取userId）
     * @return 识别结果
     */
    @PostMapping
    public ApiResponse<RecognitionResponse> recognize(
            @RequestParam("image") MultipartFile file,
            @RequestParam(value = "area", required = false) String area,
            HttpServletRequest request) {
        try {
            // 从请求属性中获取用户ID（由JWT拦截器设置）
            Integer userId = (Integer) request.getAttribute("userId");

            RecognitionResponse response = recognitionService.recognize(file, userId, area);
            return ApiResponse.success(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(e.getMessage(), "RECOGNITION_FAILED");
        }
    }
}
