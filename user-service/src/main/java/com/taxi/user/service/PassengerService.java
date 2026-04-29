package com.taxi.user.service;

import com.taxi.common.dto.PassengerDto;
import com.taxi.user.entity.Passenger;
import com.taxi.user.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PassengerService {

    private final PassengerRepository passengerRepository;

    @Transactional
    public Passenger createPassenger(PassengerDto dto) {
        Passenger passenger = new Passenger();
        passenger.setName(dto.getName());
        passenger.setEmail(dto.getEmail());
        passenger.setPhone(dto.getPhone());
        return passengerRepository.save(passenger);
    }

    public Passenger getPassengerById(Long id) {
        return passengerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Passenger not found: " + id));
    }
}