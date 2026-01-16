package com.scnai.plant.repository;

import com.scnai.plant.entity.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 对话历史数据访问层
 */
@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Integer> {
    /**
     * 根据用户ID和会话ID查询对话历史
     */
    List<ChatHistory> findByUserIdAndSessionIdOrderByCreatedAtAsc(Integer userId, String sessionId);
}
