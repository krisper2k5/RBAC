package com.taxi.trip.client;

import com.taxi.common.dto.NotificationTaskDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(name = "notification-service", url = "${notification.service.url:http://notification-service:8080}")
public interface NotificationClient {

    @PostMapping("/notifications")
    com.taxi.common.dto.NotificationTaskDto createNotification(@RequestBody NotificationTaskDto dto);

    @GetMapping("/notifications")
    List<com.taxi.common.dto.NotificationTaskDto> getNotificationsByTrip(@RequestParam("trip_id") Long tripId);
}