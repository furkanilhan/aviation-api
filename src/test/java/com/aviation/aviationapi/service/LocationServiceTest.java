package com.aviation.aviationapi.service;

import com.aviation.aviationapi.exception.BusinessException;
import com.aviation.aviationapi.mapper.LocationMapper;
import com.aviation.aviationapi.model.dto.request.LocationRequest;
import com.aviation.aviationapi.model.dto.response.LocationResponse;
import com.aviation.aviationapi.model.entity.Location;
import com.aviation.aviationapi.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private LocationMapper locationMapper;

    @InjectMocks
    private LocationService locationService;

    private Location location;
    private LocationResponse locationResponse;
    private LocationRequest locationRequest;

    @BeforeEach
    void setUp() {
        location = Location.builder()
                .id(1L)
                .name("Istanbul Airport")
                .country("Turkey")
                .city("Istanbul")
                .locationCode("IST")
                .build();

        locationResponse = LocationResponse.builder()
                .id(1L)
                .name("Istanbul Airport")
                .country("Turkey")
                .city("Istanbul")
                .locationCode("IST")
                .build();

        locationRequest = LocationRequest.builder()
                .name("Istanbul Airport")
                .country("Turkey")
                .city("Istanbul")
                .locationCode("IST")
                .build();
    }

    @Test
    void getAllLocations_ShouldReturnAllLocations() {
        when(locationRepository.findAll()).thenReturn(List.of(location));
        when(locationMapper.toResponseList(List.of(location)))
                .thenReturn(List.of(locationResponse));

        List<LocationResponse> result = locationService.getAllLocations();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLocationCode()).isEqualTo("IST");
        verify(locationRepository).findAll();
    }

    @Test
    void getLocationById_WhenExists_ShouldReturnLocation() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(locationMapper.toResponse(location)).thenReturn(locationResponse);

        LocationResponse result = locationService.getLocationById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Istanbul Airport");
    }

    @Test
    void getLocationById_WhenNotExists_ShouldThrowException() {
        when(locationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.getLocationById(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Location not found with id: 99");
    }

    @Test
    void createLocation_WhenCodeNotExists_ShouldCreateSuccessfully() {
        when(locationRepository.existsByLocationCode("IST")).thenReturn(false);
        when(locationMapper.toEntity(locationRequest)).thenReturn(location);
        when(locationRepository.save(location)).thenReturn(location);
        when(locationMapper.toResponse(location)).thenReturn(locationResponse);

        LocationResponse result = locationService.createLocation(locationRequest);

        assertThat(result.getLocationCode()).isEqualTo("IST");
        verify(locationRepository).save(location);
    }

    @Test
    void createLocation_WhenCodeExists_ShouldThrowException() {
        when(locationRepository.existsByLocationCode("IST")).thenReturn(true);

        assertThatThrownBy(() -> locationService.createLocation(locationRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Location code already exists");
    }

    @Test
    void deleteLocation_WhenExists_ShouldDelete() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));

        locationService.deleteLocation(1L);

        verify(locationRepository).deleteById(1L);
    }
}