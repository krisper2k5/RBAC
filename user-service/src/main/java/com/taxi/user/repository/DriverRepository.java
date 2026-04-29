package com.taxi.user.repository;

import com.taxi.user.entity.Driver;
import com.taxi.common.enums.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    @Query("SELECT d FROM Driver d WHERE d.status = 'AVAILABLE' ORDER BY d.createdAt ASC LIMIT 1 FOR UPDATE SKIP LOCKED")
    Optional<Driver> findFirstAvailableDriver();
}