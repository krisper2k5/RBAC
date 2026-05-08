package com.taxi.trip.controller;

import com.taxi.common.dto.TripDto;
import com.taxi.common.enums.TripStatus;
import com.taxi.trip.entity.Trip;
import com.taxi.trip.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<Trip> createTrip(@Valid @RequestBody TripDto dto) {
        return ResponseEntity.ok(tripService.createTrip(
                dto.getPassengerId(),
                dto.getOrigin(),
                dto.getDestination(),
                dto.getDistanceKm()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTrip(@PathVariable Long id) {
        return ResponseEntity.ok(tripService.getTripById(id));
    }

    @GetMapping
    public ResponseEntity<List<Trip>> getTripsByPassenger(@RequestParam Long passenger_id) {
        return ResponseEntity.ok(tripService.getTripsByPassengerId(passenger_id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Trip> updateStatus(
            @PathVariable Long id,
            @RequestParam TripStatus status) {
        return ResponseEntity.ok(tripService.updateTripStatus(id, status));
    }

    @PatchMapping("/{id}/rate")
    public ResponseEntity<Trip> rateTrip(
            @PathVariable Long id,
            @RequestParam Integer rating) {
        return ResponseEntity.ok(tripService.rateTrip(id, rating));
    }

    @GetMapping("/stats/daily")
    public ResponseEntity<Map<String, Object>> getDailyStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("tripsCount", tripService.getTripsCountToday());

        Double avgPrice = tripService.getAveragePriceToday();
        stats.put("averagePrice", avgPrice != null ? avgPrice : 0.0);

        return ResponseEntity.ok(stats);
    }
}