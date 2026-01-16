package com.scnai.plant.controller;

import com.scnai.plant.common.ApiResponse;
import com.scnai.plant.entity.RecognitionRecord;
import com.scnai.plant.repository.RecognitionRecordRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 识别记录控制器 - 处理识别历史记录查询
 */
@RestController
@RequestMapping("/records")
public class RecordController {

    @Autowired
    private RecognitionRecordRepository recordRepository;

    /**
     * 获取用户识别记录列表（分页）
     *
     * @param page 页码（从1开始）
     * @param pageSize 每页数量
     * @param request HTTP请求
     * @return 分页数据
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> getRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        try {
            // 从请求属性中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");

            // 分页查询（页码从0开始，所以要减1）
            Pageable pageable = PageRequest.of(page - 1, pageSize);
            Page<RecognitionRecord> recordPage = recordRepository.findByUserIdOrderByIdentifyTimeDesc(userId, pageable);

            // 构造响应数据
            Map<String, Object> result = new HashMap<>();
            result.put("total", recordPage.getTotalElements());
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", recordPage.getTotalPages());
            result.put("records", recordPage.getContent());

            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("查询失败");
        }
    }

    /**
     * 根据ID获取识别记录详情
     *
     * @param id 记录ID
     * @return 记录详情
     */
    @GetMapping("/{id}")
    public ApiResponse<RecognitionRecord> getRecordById(@PathVariable Integer id) {
        try {
            RecognitionRecord record = recordRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("记录不存在"));
            return ApiResponse.success(record);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取用户识别记录统计
     *
     * @param request HTTP请求
     * @return 统计数据
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats(HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");

            // 统计总数
            long totalCount = recordRepository.countByUserId(userId);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalRecognitions", totalCount);

            return ApiResponse.success(stats);
        } catch (Exception e) {
            return ApiResponse.error("统计失败");
        }
    }
}
