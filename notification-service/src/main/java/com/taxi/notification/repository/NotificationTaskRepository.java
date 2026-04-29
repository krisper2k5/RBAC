package com.taxi.notification.repository;

import com.taxi.notification.entity.NotificationTask;
import com.taxi.common.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {

    // Атомарный захват задачи: только одна транзакция получит задачу
    @Query(value = """
        SELECT * FROM notification_tasks 
        WHERE status = 'PENDING' 
        ORDER BY created_at ASC 
        LIMIT 1 
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    Optional<NotificationTask> claimNextPendingTask();

    List<NotificationTask> findByTripId(Long tripId);

    List<NotificationTask> findByStatus(NotificationStatus status);
}