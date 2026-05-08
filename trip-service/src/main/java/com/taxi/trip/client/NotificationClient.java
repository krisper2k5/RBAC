package com.taxi.trip.client;

import com.taxi.common.dto.NotificationTaskDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(
        name = "notification-service",
        url = "${notification.service.url:http://notification-service:8080}",
        configuration = com.taxi.trip.config.FeignConfig.class
)
public interface NotificationClient {

    @PostMapping("/notifications")
    NotificationTaskDto createNotification(@RequestBody NotificationTaskDto dto);

    @GetMapping("/notifications")
    List<NotificationTaskDto> getNotificationsByTrip(@RequestParam("trip_id") Long tripId);
}