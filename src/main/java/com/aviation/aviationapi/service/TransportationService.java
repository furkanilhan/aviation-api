package com.aviation.aviationapi.service;

import com.aviation.aviationapi.exception.BusinessException;
import com.aviation.aviationapi.mapper.TransportationMapper;
import com.aviation.aviationapi.model.dto.request.TransportationRequest;
import com.aviation.aviationapi.model.dto.response.TransportationResponse;
import com.aviation.aviationapi.model.entity.Transportation;
import com.aviation.aviationapi.repository.TransportationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransportationService {

    private final TransportationRepository transportationRepository;
    private final LocationService locationService;
    private final TransportationMapper transportationMapper;

    public List<TransportationResponse> getAllTransportations() {
        return transportationMapper.toResponseList(transportationRepository.findAll());
    }

    public TransportationResponse getTransportationById(Long id) {
        return transportationMapper.toResponse(findById(id));
    }

    public TransportationResponse createTransportation(TransportationRequest request) {
        validateOperatingDays(request.getOperatingDays());

        Transportation transportation = Transportation.builder()
                .originLocation(locationService.findById(request.getOriginLocationId()))
                .destinationLocation(locationService.findById(request.getDestinationLocationId()))
                .transportationType(request.getTransportationType())
                .operatingDays(request.getOperatingDays())
                .build();

        return transportationMapper.toResponse(transportationRepository.save(transportation));
    }

    public TransportationResponse updateTransportation(Long id, TransportationRequest request) {
        validateOperatingDays(request.getOperatingDays());

        Transportation existing = findById(id);
        existing.setOriginLocation(locationService.findById(request.getOriginLocationId()));
        existing.setDestinationLocation(locationService.findById(request.getDestinationLocationId()));
        existing.setTransportationType(request.getTransportationType());
        existing.setOperatingDays(request.getOperatingDays());

        return transportationMapper.toResponse(transportationRepository.save(existing));
    }

    public void deleteTransportation(Long id) {
        findById(id);
        transportationRepository.deleteById(id);
    }

    public Transportation findById(Long id) {
        return transportationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Transportation not found with id: " + id,
                        HttpStatus.NOT_FOUND
                ));
    }

    private void validateOperatingDays(List<Integer> days) {
        boolean invalid = days.stream().anyMatch(d -> d < 1 || d > 7);
        if (invalid) {
            throw new BusinessException(
                    "Operating days must be between 1 (Monday) and 7 (Sunday)",
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
