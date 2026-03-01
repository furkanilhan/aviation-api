package com.aviation.aviationapi.service;

import com.aviation.aviationapi.exception.BusinessException;
import com.aviation.aviationapi.mapper.TransportationMapper;
import com.aviation.aviationapi.model.dto.request.TransportationRequest;
import com.aviation.aviationapi.model.dto.response.TransportationResponse;
import com.aviation.aviationapi.model.entity.Transportation;
import com.aviation.aviationapi.repository.TransportationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransportationService {

    private final TransportationRepository transportationRepository;
    private final LocationService locationService;
    private final TransportationMapper transportationMapper;
    private static final String TRANSPORTATION_NOT_FOUND = "Transportation not found with id: ";

    public List<TransportationResponse> getAllTransportations() {
        return transportationMapper.toResponseList(
                transportationRepository.findAllWithLocations());
    }

    public TransportationResponse getTransportationById(Long id) {
        Transportation transportation = transportationRepository
                .findByIdWithLocations(id)
                .orElseThrow(() -> new BusinessException(
                        TRANSPORTATION_NOT_FOUND + id, HttpStatus.NOT_FOUND));
        return transportationMapper.toResponse(transportation);
    }

    @CacheEvict(value = "routes", allEntries = true)
    @Transactional
    public TransportationResponse createTransportation(TransportationRequest request) {
        Transportation transportation = Transportation.builder()
                .originLocation(locationService.findById(request.getOriginLocationId()))
                .destinationLocation(locationService.findById(request.getDestinationLocationId()))
                .transportationType(request.getTransportationType())
                .operatingDays(request.getOperatingDays())
                .build();

        transportationRepository.save(transportation);

        return transportationMapper.toResponse(
                transportationRepository.findByIdWithLocations(transportation.getId())
                        .orElseThrow());
    }

    @CacheEvict(value = "routes", allEntries = true)
    @Transactional
    public TransportationResponse updateTransportation(Long id, TransportationRequest request) {
        Transportation existing = findByIdInternal(id);
        existing.setOriginLocation(locationService.findById(request.getOriginLocationId()));
        existing.setDestinationLocation(locationService.findById(request.getDestinationLocationId()));
        existing.setTransportationType(request.getTransportationType());
        existing.setOperatingDays(request.getOperatingDays());

        transportationRepository.save(existing);

        return transportationMapper.toResponse(
                transportationRepository.findByIdWithLocations(id)
                        .orElseThrow());
    }

    @CacheEvict(value = "routes", allEntries = true)
    @Transactional
    public void deleteTransportation(Long id) {
        if (!transportationRepository.existsById(id)) {
            throw new BusinessException(
                    TRANSPORTATION_NOT_FOUND + id, HttpStatus.NOT_FOUND);
        }
        transportationRepository.deleteById(id);
    }

    private Transportation findByIdInternal(Long id) {
        return transportationRepository.findByIdWithLocations(id)
                .orElseThrow(() -> new BusinessException(
                        TRANSPORTATION_NOT_FOUND + id, HttpStatus.NOT_FOUND));
    }
}