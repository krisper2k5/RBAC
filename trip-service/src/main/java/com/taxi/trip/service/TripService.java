package com.taxi.trip.service;

import com.taxi.common.enums.TripStatus;
import com.taxi.trip.client.UserClient;
import com.taxi.trip.entity.Trip;
import com.taxi.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripService {

    private final TripRepository tripRepository;
    private final UserClient userClient;
    private final StringRedisTemplate redisTemplate;
    private static final String AVAILABLE_DRIVERS_KEY = "drivers:available";
    private final BigDecimal tariffPerKm = BigDecimal.valueOf(50.0);

    @Transactional
    public Trip createTrip(Long passengerId, String origin, String destination, Double distanceKm) {
        Trip trip = new Trip();
        trip.setPassengerId(passengerId);
        trip.setOrigin(origin);
        trip.setDestination(destination);
        trip.setDistanceKm(distanceKm);
        trip.setPrice(calculatePrice(distanceKm));
        trip.setStatus(TripStatus.CREATED);

        try {
            Boolean hasAvailable = redisTemplate.hasKey(AVAILABLE_DRIVERS_KEY);

            var driver = userClient.findAvailableDriver();
            trip.setDriverId(driver.getId());
            trip.setStatus(TripStatus.ASSIGNED);

        } catch (Exception e) {
            trip.setStatus(TripStatus.CREATED); // Нет доступных водителей
        }

        return tripRepository.save(trip);
    }

    private BigDecimal calculatePrice(Double distanceKm) {
        return tariffPerKm.multiply(BigDecimal.valueOf(distanceKm));
    }

    public Trip getTripById(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Trip> getTripsByPassengerId(Long passengerId) {
        return tripRepository.findByPassengerIdOrderByCreatedAtDesc(passengerId);
    }

    @Transactional
    public Trip updateTripStatus(Long id, TripStatus newStatus) {
        Trip trip = getTripById(id);
        trip.setStatus(newStatus);

        if (newStatus == TripStatus.COMPLETED) {
            userClient.updateDriverStatus(trip.getDriverId(), com.taxi.common.enums.DriverStatus.AVAILABLE);
        }

        return tripRepository.save(trip);
    }

    @Transactional
    public Trip rateTrip(Long id, Integer rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Trip trip = getTripById(id);
        if (trip.getStatus() != TripStatus.COMPLETED) {
            throw new IllegalStateException("Can only rate completed trips");
        }

        trip.setRating(rating);
        return tripRepository.save(trip);
    }

    public long getTripsCountToday() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return tripRepository.countByCreatedAtBetween(startOfDay, endOfDay);
    }

    public Double getAveragePriceToday() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return tripRepository.averagePriceByCreatedAtBetween(startOfDay, endOfDay);
    }
}