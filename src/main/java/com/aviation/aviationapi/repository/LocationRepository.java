package com.aviation.aviationapi.repository;

import com.aviation.aviationapi.model.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    boolean existsByLocationCode(String locationCode);
    Optional<Location> findByLocationCode(String locationCode);
}
