package com.aviation.aviationapi.repository;

import com.aviation.aviationapi.model.entity.Transportation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransportationRepository extends JpaRepository<Transportation, Long> {

    @Query("SELECT t FROM Transportation t " +
            "JOIN FETCH t.originLocation " +
            "JOIN FETCH t.destinationLocation")
    List<Transportation> findAllWithLocations();

    @Query("SELECT t FROM Transportation t " +
            "JOIN FETCH t.originLocation " +
            "JOIN FETCH t.destinationLocation " +
            "WHERE t.id = :id")
    Optional<Transportation> findByIdWithLocations(@Param("id") Long id);

    @Query("SELECT t FROM Transportation t " +
            "JOIN FETCH t.originLocation " +
            "JOIN FETCH t.destinationLocation " +
            "WHERE :day MEMBER OF t.operatingDays")
    List<Transportation> findByOperatingDayWithLocations(@Param("day") Integer day);
}
