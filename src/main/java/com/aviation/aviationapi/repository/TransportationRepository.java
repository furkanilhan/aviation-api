package com.aviation.aviationapi.repository;

import com.aviation.aviationapi.model.entity.Transportation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransportationRepository extends JpaRepository<Transportation, Long> {

    @Query("SELECT t FROM Transportation t " +
            "JOIN FETCH t.originLocation " +
            "JOIN FETCH t.destinationLocation " +
            "WHERE :day MEMBER OF t.operatingDays")
    List<Transportation> findByOperatingDayWithLocations(@Param("day") Integer day);
}
