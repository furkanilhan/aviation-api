package com.aviation.aviationapi.service;
import com.aviation.aviationapi.exception.BusinessException;
import com.aviation.aviationapi.mapper.TransportationMapper;
import com.aviation.aviationapi.model.dto.response.RouteResponse;
import com.aviation.aviationapi.model.dto.response.TransportationResponse;
import com.aviation.aviationapi.model.entity.Location;
import com.aviation.aviationapi.model.entity.Transportation;
import com.aviation.aviationapi.model.enums.TransportationType;
import com.aviation.aviationapi.repository.TransportationRepository;
import com.aviation.aviationapi.validation.RouteFilterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private TransportationRepository transportationRepository;

    @Mock
    private LocationService locationService;

    @Mock
    private TransportationMapper transportationMapper;

    @Mock
    private RouteFilterService routeFilterService; // Yeni eklenen servis

    @InjectMocks
    private RouteService routeService;

    private Location istanbul, london, taksim, wembley;
    private Transportation flight, busToAirport, busFromAirport, uberFromAirport;

    @BeforeEach
    void setUp() {
        taksim = Location.builder().id(1L).name("Taksim").country("Turkey").city("Istanbul").locationCode("TKSM").build();
        istanbul = Location.builder().id(2L).name("Istanbul Airport").country("Turkey").city("Istanbul").locationCode("IST").build();
        london = Location.builder().id(3L).name("London Heathrow").country("UK").city("London").locationCode("LHR").build();
        wembley = Location.builder().id(4L).name("Wembley").country("UK").city("London").locationCode("CCWEM").build();

        flight = Transportation.builder()
                .id(10L)
                .originLocation(istanbul)
                .destinationLocation(london)
                .transportationType(TransportationType.FLIGHT)
                .operatingDays(Set.of(1, 2, 3, 4, 5, 6, 7))
                .build();

        busToAirport = Transportation.builder()
                .id(20L)
                .originLocation(taksim)
                .destinationLocation(istanbul)
                .transportationType(TransportationType.BUS)
                .operatingDays(Set.of(1, 2, 3, 4, 5, 6, 7))
                .build();

        busFromAirport = Transportation.builder()
                .id(30L)
                .originLocation(london)
                .destinationLocation(wembley)
                .transportationType(TransportationType.BUS)
                .operatingDays(Set.of(1, 2, 3, 4, 5, 6, 7))
                .build();

        uberFromAirport = Transportation.builder()
                .id(40L)
                .originLocation(london)
                .destinationLocation(wembley)
                .transportationType(TransportationType.UBER)
                .operatingDays(Set.of(1, 2, 3, 4, 5, 6, 7))
                .build();
    }

    @Test
    void findRoutes_DirectFlight_ShouldReturnOneRoute() {
        LocalDate date = LocalDate.of(2025, 3, 10);

        when(locationService.findById(anyLong())).thenReturn(new Location());
        when(transportationRepository.findByOperatingDayWithLocations(1))
                .thenReturn(List.of(flight));
        when(routeFilterService.isValidRoute(null, flight, null)).thenReturn(true);
        when(transportationMapper.toResponse(any(Transportation.class))).thenReturn(new TransportationResponse());

        List<RouteResponse> routes = routeService.findRoutes(istanbul.getId(), london.getId(), date);

        assertThat(routes).hasSize(1);
        verify(routeFilterService).isValidRoute(null, flight, null);
    }

    @Test
    void findRoutes_WithBeforeAndAfterTransfer_ShouldReturnAllCombinations() {
        LocalDate date = LocalDate.of(2025, 3, 10);
        when(locationService.findById(anyLong())).thenReturn(new Location());
        when(transportationRepository.findByOperatingDayWithLocations(1))
                .thenReturn(List.of(flight, busToAirport, busFromAirport, uberFromAirport));
        when(routeFilterService.isValidRoute(any(), eq(flight), any())).thenReturn(true);
        when(transportationMapper.toResponse(any(Transportation.class))).thenReturn(new TransportationResponse());

        List<RouteResponse> routes = routeService.findRoutes(taksim.getId(), wembley.getId(), date);

        assertThat(routes).hasSize(2);

        verify(routeFilterService, atLeast(2)).isValidRoute(any(), eq(flight), any());
    }

    @Test
    void findRoutes_WhenFilterReturnsFalse_ShouldNotAddRoute() {
        LocalDate date = LocalDate.of(2025, 3, 10);
        when(locationService.findById(anyLong())).thenReturn(new Location());
        when(transportationRepository.findByOperatingDayWithLocations(1))
                .thenReturn(List.of(flight));
        when(routeFilterService.isValidRoute(null, flight, null)).thenReturn(false);

        List<RouteResponse> routes = routeService.findRoutes(istanbul.getId(), london.getId(), date);

        assertThat(routes).isEmpty();
    }

    @Test
    void findRoutes_OnlyBeforeFlight_ShouldReturnRoute() {
        LocalDate date = LocalDate.of(2025, 3, 10);
        when(locationService.findById(anyLong())).thenReturn(new Location());
        when(transportationRepository.findByOperatingDayWithLocations(1))
                .thenReturn(List.of(flight, busToAirport));

        when(routeFilterService.isValidRoute(eq(busToAirport), eq(flight), isNull())).thenReturn(true);
        when(transportationMapper.toResponse(any(Transportation.class))).thenReturn(new TransportationResponse());

        List<RouteResponse> routes = routeService.findRoutes(taksim.getId(), london.getId(), date);

        assertThat(routes).hasSize(1);
    }

    @Test
    void findRoutes_SameOriginAndDestination_ShouldThrowException() {
        assertThatThrownBy(() ->
                routeService.findRoutes(1L, 1L, LocalDate.now())
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Origin and destination cannot be the same");

        verifyNoInteractions(transportationRepository);
    }

    @Test
    void findRoutes_NoActiveTransportations_ShouldReturnEmpty() {
        LocalDate date = LocalDate.of(2025, 3, 9); // Pazar günü
        when(locationService.findById(anyLong())).thenReturn(new Location());
        when(transportationRepository.findByOperatingDayWithLocations(anyInt()))
                .thenReturn(Collections.emptyList());

        List<RouteResponse> routes = routeService.findRoutes(1L, 4L, date);

        assertThat(routes).isEmpty();
        verifyNoInteractions(routeFilterService);
    }
}