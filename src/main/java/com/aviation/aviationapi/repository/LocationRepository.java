package com.aviation.aviationapi.repository;

import com.aviation.aviationapi.model.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    boolean existsByLocationCode(String locationCode);

    List<Location> findAllByOrderByIdDesc();
}
