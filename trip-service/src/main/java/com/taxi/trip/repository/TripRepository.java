package com.taxi.trip.repository;

import com.taxi.trip.entity.Trip;
import com.taxi.common.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);List<Trip> findByPassengerId(Long passengerId);

    @Query("SELECT COUNT(t) FROM Trip t WHERE t.createdAt BETWEEN :start AND :end")
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT AVG(t.price) FROM Trip t WHERE t.createdAt BETWEEN :start AND :end")
    Double averagePriceByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}