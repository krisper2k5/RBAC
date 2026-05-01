package com.taxi.trip.service;

import com.taxi.common.dto.NotificationTaskDto;
import com.taxi.common.enums.DriverStatus;
import com.taxi.common.enums.RecipientType;
import com.taxi.common.enums.TripStatus;
import com.taxi.trip.client.NotificationClient;
import com.taxi.trip.client.UserClient;
import com.taxi.trip.entity.Trip;
import com.taxi.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final NotificationClient notificationClient;
    private final StringRedisTemplate redisTemplate;

    @Value("${app.tariff-per-km:50.0}")
    private double tariffPerKm;

    private static final String AVAILABLE_DRIVERS_KEY = "drivers:available";

    @Transactional
    public Trip createTrip(Long passengerId, String origin, String destination, Double distanceKm) {
        userClient.validatePassengerExists(passengerId);

        Trip trip = new Trip();
        trip.setPassengerId(passengerId);
        trip.setOrigin(origin);
        trip.setDestination(destination);
        trip.setDistanceKm(distanceKm);
        trip.setPrice(calculatePrice(distanceKm));
        trip.setStatus(TripStatus.CREATED);

        try {
            var driver = userClient.findAvailableDriver();
            trip.setDriverId(driver.getId());
            trip.setStatus(TripStatus.ASSIGNED);

            sendNotification(trip.getId(), RecipientType.DRIVER, driver.getId(),
                    "Новая поездка: " + origin + " → " + destination);

        } catch (Exception e) {
            trip.setStatus(TripStatus.CREATED);
            sendNotification(trip.getId(), RecipientType.PASSENGER, passengerId,
                    "Поиск водителя... Поездка в очереди");
        }

        return tripRepository.save(trip);
    }

    private void sendNotification(Long tripId, RecipientType type, Long recipientId, String message) {
        try {
            NotificationTaskDto dto = new NotificationTaskDto();
            dto.setTripId(tripId);
            dto.setRecipientType(type);
            dto.setRecipientId(recipientId);
            dto.setMessage(message);
            notificationClient.createNotification(dto);
        } catch (Exception e) {
        }
    }

    private BigDecimal calculatePrice(Double distanceKm) {
        return BigDecimal.valueOf(tariffPerKm).multiply(BigDecimal.valueOf(distanceKm));
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
        TripStatus oldStatus = trip.getStatus();
        trip.setStatus(newStatus);

        // Уведомления при смене статуса
        if (trip.getDriverId() != null) {
            String msg = switch (newStatus) {
                case IN_PROGRESS -> "Поездка началась";
                case COMPLETED -> "Поездка завершена. Спасибо!";
                case CANCELLED -> "Поездка отменена";
                default -> null;
            };
            if (msg != null) {
                sendNotification(trip.getId(), RecipientType.DRIVER, trip.getDriverId(), msg);
                sendNotification(trip.getId(), RecipientType.PASSENGER, trip.getPassengerId(), msg);
            }
        }

        // Освобождаем водителя при завершении
        if (newStatus == TripStatus.COMPLETED) {
            userClient.updateDriverStatus(trip.getDriverId(), DriverStatus.AVAILABLE);
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