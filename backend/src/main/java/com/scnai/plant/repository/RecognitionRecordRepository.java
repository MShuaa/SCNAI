package com.scnai.plant.repository;

import com.scnai.plant.entity.RecognitionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 识别记录数据访问层
 */
@Repository
public interface RecognitionRecordRepository extends JpaRepository<RecognitionRecord, Integer> {
    /**
     * 根据用户ID分页查询识别记录
     */
    Page<RecognitionRecord> findByUserIdOrderByIdentifyTimeDesc(Integer userId, Pageable pageable);

    /**
     * 统计用户识别记录总数
     */
    long countByUserId(Integer userId);
}
