package com.aviation.aviationapi.service;

import com.aviation.aviationapi.exception.BusinessException;
import com.aviation.aviationapi.mapper.TransportationMapper;
import com.aviation.aviationapi.model.dto.response.RouteResponse;
import com.aviation.aviationapi.model.entity.Transportation;
import com.aviation.aviationapi.model.enums.TransportationType;
import com.aviation.aviationapi.repository.TransportationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
        List<Transportation> activeTransports =
                transportationRepository.findByOperatingDayWithLocations(dayOfWeek);

        if (activeTransports.isEmpty()) {
            log.debug("No active transports found for day: {}", dayOfWeek);
            return Collections.emptyList();
        }

        List<Transportation> flights = activeTransports.stream()
                .filter(t -> t.getTransportationType() == TransportationType.FLIGHT)
                .toList();

        Map<Long, List<Transportation>> nonFlightsByDestination = activeTransports.stream()
                .filter(t -> t.getTransportationType() != TransportationType.FLIGHT)
                .collect(Collectors.groupingBy(t -> t.getDestinationLocation().getId()));

        Map<Long, List<Transportation>> nonFlightsByOrigin = activeTransports.stream()
                .filter(t -> t.getTransportationType() != TransportationType.FLIGHT)
                .collect(Collectors.groupingBy(t -> t.getOriginLocation().getId()));

        List<RouteResponse> routes = new ArrayList<>();
        for (Transportation flight : flights) {
            routes.addAll(buildRoutesForFlight(
                    flight, originId, destinationId,
                    nonFlightsByDestination, nonFlightsByOrigin));
        }

        log.debug("Found {} routes from {} to {} on {}",
                routes.size(), originId, destinationId, date);

        return routes;
    }

    private List<RouteResponse> buildRoutesForFlight(
            Transportation flight,
            Long originId,
            Long destinationId,
            Map<Long, List<Transportation>> nonFlightsByDestination,
            Map<Long, List<Transportation>> nonFlightsByOrigin) {

        Long flightOriginId = flight.getOriginLocation().getId();
        Long flightDestId = flight.getDestinationLocation().getId();

        boolean flightStartsAtOrigin = flightOriginId.equals(originId);
        boolean flightEndsAtDestination = flightDestId.equals(destinationId);

        List<Transportation> beforeOptions = nonFlightsByDestination
                .getOrDefault(flightOriginId, Collections.emptyList())
                .stream()
                .filter(t -> t.getOriginLocation().getId().equals(originId))
                .toList();

        List<Transportation> afterOptions = nonFlightsByOrigin
                .getOrDefault(flightDestId, Collections.emptyList())
                .stream()
                .filter(t -> t.getDestinationLocation().getId().equals(destinationId))
                .toList();

        List<RouteResponse> routes = new ArrayList<>();

        if (flightStartsAtOrigin && flightEndsAtDestination) {
            routes.add(buildRoute(null, flight, null));
        }

        if (flightEndsAtDestination) {
            beforeOptions.forEach(before -> routes.add(buildRoute(before, flight, null)));
        }

        if (flightStartsAtOrigin) {
            afterOptions.forEach(after -> routes.add(buildRoute(null, flight, after)));
        }

        for (Transportation before : beforeOptions) {
            for (Transportation after : afterOptions) {
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