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

    List<Trip> findByPassengerId(Long passengerId);

    @Query("SELECT COUNT(t) FROM Trip t WHERE t.createdAt >= :startOfDay")
    long countTripsToday(LocalDateTime startOfDay);

    @Query("SELECT AVG(t.price) FROM Trip t WHERE t.createdAt >= :startOfDay")
    Double averagePriceToday(LocalDateTime startOfDay);
}