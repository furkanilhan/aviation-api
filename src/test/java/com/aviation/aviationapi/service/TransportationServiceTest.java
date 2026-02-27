package com.aviation.aviationapi.service;

import com.aviation.aviationapi.exception.BusinessException;
import com.aviation.aviationapi.mapper.TransportationMapper;
import com.aviation.aviationapi.model.dto.request.TransportationRequest;
import com.aviation.aviationapi.model.dto.response.TransportationResponse;
import com.aviation.aviationapi.model.entity.Location;
import com.aviation.aviationapi.model.entity.Transportation;
import com.aviation.aviationapi.model.enums.TransportationType;
import com.aviation.aviationapi.repository.TransportationRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransportationServiceTest {

    @Mock
    private TransportationRepository transportationRepository;

    @Mock
    private LocationService locationService;

    @Mock
    private TransportationMapper transportationMapper;

    @InjectMocks
    private TransportationService transportationService;

    private Location origin, destination;
    private Transportation transportation;
    private TransportationResponse transportationResponse;
    private TransportationRequest transportationRequest;

    @BeforeEach
    void setUp() {
        origin = Location.builder()
                .id(1L).name("Istanbul Airport").locationCode("IST").build();

        destination = Location.builder()
                .id(2L).name("London Heathrow").locationCode("LHR").build();

        transportation = Transportation.builder()
                .id(1L)
                .originLocation(origin)
                .destinationLocation(destination)
                .transportationType(TransportationType.FLIGHT)
                .operatingDays(List.of(1, 2, 3, 4, 5))
                .build();

        transportationResponse = TransportationResponse.builder()
                .id(1L)
                .transportationType(TransportationType.FLIGHT)
                .operatingDays(List.of(1, 2, 3, 4, 5))
                .build();

        transportationRequest = TransportationRequest.builder()
                .originLocationId(1L)
                .destinationLocationId(2L)
                .transportationType(TransportationType.FLIGHT)
                .operatingDays(List.of(1, 2, 3, 4, 5))
                .build();
    }

    @Test
    void getAllTransportations_ShouldReturnAll() {
        when(transportationRepository.findAll()).thenReturn(List.of(transportation));
        when(transportationMapper.toResponseList(List.of(transportation)))
                .thenReturn(List.of(transportationResponse));

        List<TransportationResponse> result = transportationService.getAllTransportations();

        assertThat(result).hasSize(1);
        verify(transportationRepository).findAll();
    }

    @Test
    void getTransportationById_WhenExists_ShouldReturn() {
        when(transportationRepository.findById(1L)).thenReturn(Optional.of(transportation));
        when(transportationMapper.toResponse(transportation)).thenReturn(transportationResponse);

        TransportationResponse result = transportationService.getTransportationById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTransportationType()).isEqualTo(TransportationType.FLIGHT);
    }

    @Test
    void getTransportationById_WhenNotExists_ShouldThrowException() {
        when(transportationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transportationService.getTransportationById(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Transportation not found with id: 99");
    }

    @Test
    void createTransportation_ShouldCreateSuccessfully() {
        when(locationService.findById(1L)).thenReturn(origin);
        when(locationService.findById(2L)).thenReturn(destination);
        when(transportationRepository.save(any())).thenReturn(transportation);
        when(transportationMapper.toResponse(transportation)).thenReturn(transportationResponse);

        TransportationResponse result = transportationService.createTransportation(transportationRequest);

        assertThat(result.getTransportationType()).isEqualTo(TransportationType.FLIGHT);
        verify(transportationRepository).save(any());
    }

    @Test
    void createTransportation_WithInvalidOperatingDays_ShouldThrowException() {
        TransportationRequest invalidRequest = TransportationRequest.builder()
                .originLocationId(1L)
                .destinationLocationId(2L)
                .transportationType(TransportationType.FLIGHT)
                .operatingDays(List.of(0, 8)) // 0 ve 8 geçersiz
                .build();

        assertThatThrownBy(() -> transportationService.createTransportation(invalidRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Operating days must be between 1");
    }

    @Test
    void deleteTransportation_WhenExists_ShouldDelete() {
        when(transportationRepository.findById(1L)).thenReturn(Optional.of(transportation));

        transportationService.deleteTransportation(1L);

        verify(transportationRepository).deleteById(1L);
    }

    @Test
    void deleteTransportation_WhenNotExists_ShouldThrowException() {
        when(transportationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transportationService.deleteTransportation(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Transportation not found with id: 99");
    }
}