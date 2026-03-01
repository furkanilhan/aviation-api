package com.aviation.aviationapi.controller;

import com.aviation.aviationapi.model.dto.response.LocationResponse;
import com.aviation.aviationapi.model.dto.response.RouteResponse;
import com.aviation.aviationapi.service.LocationService;
import com.aviation.aviationapi.service.RouteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
@Validated
@Tag(name = "Routes", description = "Route search endpoints")
public class RouteController {

    private final RouteService routeService;
    private final LocationService locationService;

    @GetMapping
    public ResponseEntity<List<RouteResponse>> findRoutes(
            @RequestParam @Min(value = 1, message = "Origin ID must be valid") Long originId,
            @RequestParam @Min(value = 1, message = "Destination ID must be valid") Long destinationId,
            @RequestParam @NotNull @FutureOrPresent(message = "Date must be today or in the future")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("Route search request received. Origin: {}, Dest: {}, Date: {}",
                originId, destinationId, date);
        return ResponseEntity.ok(routeService.findRoutes(originId, destinationId, date));
    }

    @GetMapping("/locations")
    public ResponseEntity<List<LocationResponse>> getLocationsForRoute() {
        return ResponseEntity.ok(locationService.getAllLocations());
    }
}
