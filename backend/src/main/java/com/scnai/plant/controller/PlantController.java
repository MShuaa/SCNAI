package com.scnai.plant.controller;

import com.scnai.plant.common.ApiResponse;
import com.scnai.plant.entity.Plant;
import com.scnai.plant.repository.PlantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 植物信息控制器 - 处理植物信息查询
 */
@RestController
@RequestMapping("/plants")
public class PlantController {

    @Autowired
    private PlantRepository plantRepository;

    /**
     * 获取所有植物列表
     *
     * @return 植物列表
     */
    @GetMapping
    public ApiResponse<List<Plant>> getAllPlants() {
        try {
            List<Plant> plants = plantRepository.findAll();
            return ApiResponse.success(plants);
        } catch (Exception e) {
            return ApiResponse.error("查询失败");
        }
    }

    /**
     * 根据ID获取植物详情
     *
     * @param id 植物ID
     * @return 植物详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Plant> getPlantById(@PathVariable Integer id) {
        try {
            Plant plant = plantRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("植物不存在"));
            return ApiResponse.success(plant);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据名称查询植物
     *
     * @param name 植物名称
     * @return 植物详情
     */
    @GetMapping("/search")
    public ApiResponse<Plant> searchPlantByName(@RequestParam String name) {
        try {
            Plant plant = plantRepository.findByName(name)
                    .orElseThrow(() -> new RuntimeException("植物不存在"));
            return ApiResponse.success(plant);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 模糊搜索植物
     *
     * @param keyword 关键词
     * @return 植物列表
     */
    @GetMapping("/search/keyword")
    public ApiResponse<List<Plant>> searchPlants(@RequestParam String keyword) {
        try {
            List<Plant> plants = plantRepository.findByNameContaining(keyword);
            return ApiResponse.success(plants);
        } catch (Exception e) {
            return ApiResponse.error("搜索失败");
        }
    }
}
