package com.aviation.aviationapi.service;

import com.aviation.aviationapi.exception.BusinessException;
import com.aviation.aviationapi.mapper.LocationMapper;
import com.aviation.aviationapi.model.dto.request.LocationRequest;
import com.aviation.aviationapi.model.dto.response.LocationResponse;
import com.aviation.aviationapi.model.entity.Location;
import com.aviation.aviationapi.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    public List<LocationResponse> getAllLocations() {
        return locationMapper.toResponseList(locationRepository.findAll());
    }

    public LocationResponse getLocationById(Long id) {
        return locationMapper.toResponse(findById(id));
    }

    public LocationResponse createLocation(LocationRequest request) {
        if (locationRepository.existsByLocationCode(request.getLocationCode())) {
            throw new BusinessException(
                    "Location code already exists: " + request.getLocationCode(),
                    HttpStatus.CONFLICT
            );
        }
        Location location = locationMapper.toEntity(request);
        return locationMapper.toResponse(locationRepository.save(location));
    }

    public LocationResponse updateLocation(Long id, LocationRequest request) {
        Location existing = findById(id);

        if (!existing.getLocationCode().equals(request.getLocationCode()) &&
                locationRepository.existsByLocationCode(request.getLocationCode())) {
            throw new BusinessException(
                    "Location code already exists: " + request.getLocationCode(),
                    HttpStatus.CONFLICT
            );
        }

        existing.setName(request.getName());
        existing.setCountry(request.getCountry());
        existing.setCity(request.getCity());
        existing.setLocationCode(request.getLocationCode());

        return locationMapper.toResponse(locationRepository.save(existing));
    }

    public void deleteLocation(Long id) {
        findById(id);
        locationRepository.deleteById(id);
    }

    public Location findById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Location not found with id: " + id,
                        HttpStatus.NOT_FOUND
                ));
    }
}
