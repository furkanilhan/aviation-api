package com.aviation.aviationapi.service;

import com.aviation.aviationapi.exception.BusinessException;
import com.aviation.aviationapi.mapper.TransportationMapper;
import com.aviation.aviationapi.model.dto.response.RouteResponse;
import com.aviation.aviationapi.model.entity.Transportation;
import com.aviation.aviationapi.repository.TransportationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteService {

    private final TransportationRepository transportationRepository;
    private final LocationService locationService;
    private final TransportationMapper transportationMapper;

    @Cacheable(value = "routes", key = "#originId + '-' + #destinationId + '-' + #date")
    public List<RouteResponse> findRoutes(Long originId, Long destinationId, LocalDate date) {

        if (originId.equals(destinationId)) {
            throw new BusinessException(
                    "Origin and destination cannot be the same", HttpStatus.BAD_REQUEST);
        }
        locationService.findById(originId);
        locationService.findById(destinationId);

        int dayOfWeek = date.getDayOfWeek().getValue();

        List<Transportation> beforeCandidates =
                transportationRepository.findBeforeCandidates(dayOfWeek, originId);

        List<Transportation> afterCandidates =
                transportationRepository.findAfterCandidates(dayOfWeek, destinationId);

        Set<Long> flightOriginIds = beforeCandidates.stream()
                .map(t -> t.getDestinationLocation().getId())
                .collect(Collectors.toSet());
        flightOriginIds.add(originId);

        Set<Long> flightDestIds = afterCandidates.stream()
                .map(t -> t.getOriginLocation().getId())
                .collect(Collectors.toSet());
        flightDestIds.add(destinationId);

        List<Transportation> flights =
                transportationRepository.findFlightCandidates(
                        dayOfWeek, flightOriginIds, flightDestIds);

        if (flights.isEmpty()) {
            log.debug("No flights found for day: {}", dayOfWeek);
            return Collections.emptyList();
        }

        log.debug("Candidates — before: {}, flights: {}, after: {}",
                beforeCandidates.size(), flights.size(), afterCandidates.size());

        List<RouteResponse> routes = new ArrayList<>();
        for (Transportation flight : flights) {
            routes.addAll(buildRoutesForFlight(
                    flight, originId, destinationId,
                    beforeCandidates, afterCandidates));
        }

        log.debug("Found {} routes from {} to {} on {}",
                routes.size(), originId, destinationId, date);

        return routes;
    }

    private List<RouteResponse> buildRoutesForFlight(
            Transportation flight,
            Long originId,
            Long destinationId,
            List<Transportation> beforeCandidates,
            List<Transportation> afterCandidates) {

        Long flightOriginId = flight.getOriginLocation().getId();
        Long flightDestId = flight.getDestinationLocation().getId();

        List<Transportation> beforeOptions = beforeCandidates.stream()
                .filter(t -> t.getDestinationLocation().getId().equals(flightOriginId))
                .toList();

        List<Transportation> afterOptions = afterCandidates.stream()
                .filter(t -> t.getOriginLocation().getId().equals(flightDestId))
                .toList();

        List<Transportation> beforeWithNull = new ArrayList<>();
        if (flightOriginId.equals(originId)) beforeWithNull.add(null);
        beforeWithNull.addAll(beforeOptions);

        List<Transportation> afterWithNull = new ArrayList<>();
        if (flightDestId.equals(destinationId)) afterWithNull.add(null);
        afterWithNull.addAll(afterOptions);

        List<RouteResponse> routes = new ArrayList<>();
        for (Transportation before : beforeWithNull) {
            for (Transportation after : afterWithNull) {
                routes.add(buildRoute(before, flight, after));
            }
        }

        return routes;
    }

    private RouteResponse buildRoute(Transportation before,
                                     Transportation flight,
                                     Transportation after) {
        return RouteResponse.builder()
                .beforeFlight(before != null ? transportationMapper.toResponse(before) : null)
                .flight(transportationMapper.toResponse(flight))
                .afterFlight(after != null ? transportationMapper.toResponse(after) : null)
                .build();
    }
}