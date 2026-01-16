package com.scnai.plant.repository;

import com.scnai.plant.entity.Plant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

/**
 * 植物信息数据访问层
 */
@Repository
public interface PlantRepository extends JpaRepository<Plant, Integer> {
    /**
     * 根据名称查询植物
     */
    Optional<Plant> findByName(String name);

    /**
     * 模糊搜索植物名称
     */
    List<Plant> findByNameContaining(String keyword);
}
