package com.aviation.aviationapi.repository;

import com.aviation.aviationapi.model.entity.Transportation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TransportationRepository extends JpaRepository<Transportation, Long> {

    @Query("SELECT DISTINCT t FROM Transportation t " +
            "JOIN FETCH t.originLocation " +
            "JOIN FETCH t.destinationLocation " +
            "LEFT JOIN FETCH t.operatingDays " +
            "ORDER BY t.id DESC")
    List<Transportation> findAllWithLocations();

    @Query("SELECT t FROM Transportation t " +
            "JOIN FETCH t.originLocation " +
            "JOIN FETCH t.destinationLocation " +
            "LEFT JOIN FETCH t.operatingDays " +
            "WHERE t.id = :id")
    Optional<Transportation> findByIdWithLocations(@Param("id") Long id);

    @Query("SELECT DISTINCT t FROM Transportation t " +
            "JOIN FETCH t.originLocation " +
            "JOIN FETCH t.destinationLocation " +
            "WHERE :day MEMBER OF t.operatingDays " +
            "AND t.transportationType != com.aviation.aviationapi.model.enums.TransportationType.FLIGHT " +
            "AND t.originLocation.id = :originId")
    List<Transportation> findBeforeCandidates(
            @Param("day") int day,
            @Param("originId") Long originId);

    @Query("SELECT DISTINCT t FROM Transportation t " +
            "JOIN FETCH t.originLocation " +
            "JOIN FETCH t.destinationLocation " +
            "WHERE :day MEMBER OF t.operatingDays " +
            "AND t.transportationType != com.aviation.aviationapi.model.enums.TransportationType.FLIGHT " +
            "AND t.destinationLocation.id = :destinationId")
    List<Transportation> findAfterCandidates(
            @Param("day") int day,
            @Param("destinationId") Long destinationId);

    @Query("SELECT DISTINCT t FROM Transportation t " +
            "JOIN FETCH t.originLocation " +
            "JOIN FETCH t.destinationLocation " +
            "WHERE :day MEMBER OF t.operatingDays " +
            "AND t.transportationType = com.aviation.aviationapi.model.enums.TransportationType.FLIGHT " +
            "AND t.originLocation.id IN :originIds " +
            "AND t.destinationLocation.id IN :destIds")
    List<Transportation> findFlightCandidates(
            @Param("day") int day,
            @Param("originIds") Set<Long> originIds,
            @Param("destIds") Set<Long> destIds);
}