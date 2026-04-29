package com.taxi.user.controller;

import com.taxi.common.dto.PassengerDto;
import com.taxi.user.entity.Passenger;
import com.taxi.user.service.PassengerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/passengers")
@RequiredArgsConstructor
public class PassengerController {

    private final PassengerService passengerService;

    @PostMapping
    public ResponseEntity<Passenger> createPassenger(@Valid @RequestBody PassengerDto dto) {
        return ResponseEntity.ok(passengerService.createPassenger(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Passenger> getPassenger(@PathVariable Long id) {
        return ResponseEntity.ok(passengerService.getPassengerById(id));
    }
}