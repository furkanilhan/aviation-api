package com.aviation.aviationapi.service;

import com.aviation.aviationapi.mapper.TransportationMapper;
import com.aviation.aviationapi.model.dto.response.RouteResponse;
import com.aviation.aviationapi.model.entity.Transportation;
import com.aviation.aviationapi.model.enums.TransportationType;
import com.aviation.aviationapi.repository.TransportationRepository;
import org.springframework.cache.annotation.Cacheable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final TransportationRepository transportationRepository;
    private final LocationService locationService;
    private final TransportationMapper transportationMapper;

    @Cacheable(value = "routes", key = "#originId + '-' + #destinationId + '-' + #date")
    public List<RouteResponse> findRoutes(Long originId, Long destinationId, LocalDate date) {
        // Lokasyonların var olduğunu doğrula
        locationService.findById(originId);
        locationService.findById(destinationId);

        int dayOfWeek = date.getDayOfWeek().getValue(); // 1=Mon, 7=Sun

        // O gün aktif olan tüm transportları çek
        List<Transportation> activeTransports =
                transportationRepository.findByOperatingDayWithLocations(dayOfWeek);

        // Sadece uçuşları al
        List<Transportation> flights = activeTransports.stream()
                .filter(t -> t.getTransportationType() == TransportationType.FLIGHT)
                .toList();

        // Non-flight transportlar
        List<Transportation> nonFlights = activeTransports.stream()
                .filter(t -> t.getTransportationType() != TransportationType.FLIGHT)
                .toList();

        List<RouteResponse> routes = new ArrayList<>();

        for (Transportation flight : flights) {
            // Bu flight origin ile destination'ı match etmiyor, skip
            // (flight zincirinin originId'den destinationId'ye gitmesi lazım
            // ama direkt veya transferle)

            // Before flight: originId -> flight.origin arasındaki non-flight'lar
            List<Transportation> beforeOptions = nonFlights.stream()
                    .filter(t -> t.getOriginLocation().getId().equals(originId))
                    .filter(t -> t.getDestinationLocation().getId()
                            .equals(flight.getOriginLocation().getId()))
                    .toList();

            // After flight: flight.destination -> destinationId arasındaki non-flight'lar
            List<Transportation> afterOptions = nonFlights.stream()
                    .filter(t -> t.getOriginLocation().getId()
                            .equals(flight.getDestinationLocation().getId()))
                    .filter(t -> t.getDestinationLocation().getId().equals(destinationId))
                    .toList();

            boolean flightStartsAtOrigin =
                    flight.getOriginLocation().getId().equals(originId);
            boolean flightEndsAtDestination =
                    flight.getDestinationLocation().getId().equals(destinationId);

            // 1. Sadece flight (origin -> destination direkt uçuş)
            if (flightStartsAtOrigin && flightEndsAtDestination) {
                routes.add(RouteResponse.builder()
                        .flight(transportationMapper.toResponse(flight))
                        .build());
            }

            // 2. before + flight (transfer -> uçuş -> destination)
            if (flightEndsAtDestination) {
                for (Transportation before : beforeOptions) {
                    routes.add(RouteResponse.builder()
                            .beforeFlight(transportationMapper.toResponse(before))
                            .flight(transportationMapper.toResponse(flight))
                            .build());
                }
            }

            // 3. flight + after (origin -> uçuş -> transfer)
            if (flightStartsAtOrigin) {
                for (Transportation after : afterOptions) {
                    routes.add(RouteResponse.builder()
                            .flight(transportationMapper.toResponse(flight))
                            .afterFlight(transportationMapper.toResponse(after))
                            .build());
                }
            }

            // 4. before + flight + after
            for (Transportation before : beforeOptions) {
                for (Transportation after : afterOptions) {
                    routes.add(RouteResponse.builder()
                            .beforeFlight(transportationMapper.toResponse(before))
                            .flight(transportationMapper.toResponse(flight))
                            .afterFlight(transportationMapper.toResponse(after))
                            .build());
                }
            }
        }

        return routes;
    }
}
