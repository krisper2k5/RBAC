package com.taxi.notification.controller;

import com.taxi.common.dto.NotificationTaskDto;
import com.taxi.common.enums.NotificationStatus;
import com.taxi.notification.entity.NotificationTask;
import com.taxi.notification.repository.NotificationTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationTaskRepository taskRepository;

    @PostMapping
    public ResponseEntity<NotificationTask> createNotification(@RequestBody NotificationTaskDto dto) {
        NotificationTask task = new NotificationTask();
        task.setTripId(dto.getTripId());
        task.setRecipientType(dto.getRecipientType());
        task.setRecipientId(dto.getRecipientId());
        task.setMessage(dto.getMessage());
        task.setStatus(NotificationStatus.PENDING);
        task.setAttempts(0);
        return ResponseEntity.ok(taskRepository.save(task));
    }

    @GetMapping
    public ResponseEntity<List<NotificationTask>> getNotificationsByTrip(@RequestParam Long trip_id) {
        return ResponseEntity.ok(taskRepository.findByTripId(trip_id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<NotificationTask>> getByStatus(@PathVariable NotificationStatus status) {
        return ResponseEntity.ok(taskRepository.findByStatus(status));
    }
}