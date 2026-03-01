package com.aviation.aviationapi.controller;

import com.aviation.aviationapi.model.dto.request.TransportationRequest;
import com.aviation.aviationapi.model.dto.response.TransportationResponse;
import com.aviation.aviationapi.service.TransportationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transportations")
@RequiredArgsConstructor
@Tag(name = "Transportations", description = "Transportation management endpoints")
@Validated
public class TransportationController {

    private final TransportationService transportationService;

    @GetMapping
    public ResponseEntity<List<TransportationResponse>> getAllTransportations() {
        return ResponseEntity.ok(transportationService.getAllTransportations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransportationResponse> getTransportationById(
            @PathVariable @Min(1) Long id) {
        return ResponseEntity.ok(transportationService.getTransportationById(id));
    }

    @PostMapping
    public ResponseEntity<TransportationResponse> createTransportation(
            @Valid @RequestBody TransportationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transportationService.createTransportation(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransportationResponse> updateTransportation(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody TransportationRequest request) {
        return ResponseEntity.ok(transportationService.updateTransportation(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransportation(
            @PathVariable @Min(1) Long id) {
        transportationService.deleteTransportation(id);
        return ResponseEntity.noContent().build();
    }
}