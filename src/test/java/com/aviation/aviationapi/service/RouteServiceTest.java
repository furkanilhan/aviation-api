package com.aviation.aviationapi.service;
import com.aviation.aviationapi.exception.BusinessException;
import com.aviation.aviationapi.mapper.TransportationMapper;
import com.aviation.aviationapi.model.dto.response.RouteResponse;
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

    @InjectMocks
    private RouteService routeService;

    private Location istanbul, london, taksim, wembley;
    private Transportation flight, busToAirport, uberToAirport, busFromAirport, uberFromAirport;

    @BeforeEach
    void setUp() {
        taksim   = Location.builder().id(1L).name("Taksim").country("Turkey").city("Istanbul").locationCode("TKSM").build();
        istanbul = Location.builder().id(2L).name("Istanbul Airport").country("Turkey").city("Istanbul").locationCode("IST").build();
        london   = Location.builder().id(3L).name("London Heathrow").country("UK").city("London").locationCode("LHR").build();
        wembley  = Location.builder().id(4L).name("Wembley").country("UK").city("London").locationCode("CCWEM").build();

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

        uberToAirport = Transportation.builder()
                .id(30L)
                .originLocation(taksim)
                .destinationLocation(istanbul)
                .transportationType(TransportationType.UBER)
                .operatingDays(Set.of(1, 2, 3, 4, 5, 6, 7))
                .build();

        busFromAirport = Transportation.builder()
                .id(40L)
                .originLocation(london)
                .destinationLocation(wembley)
                .transportationType(TransportationType.BUS)
                .operatingDays(Set.of(1, 2, 3, 4, 5, 6, 7))
                .build();

        uberFromAirport = Transportation.builder()
                .id(50L)
                .originLocation(london)
                .destinationLocation(wembley)
                .transportationType(TransportationType.UBER)
                .operatingDays(Set.of(1, 2, 3, 4, 5, 6, 7))
                .build();
    }

    private void mockMapper() {
        when(transportationMapper.toResponse(any(Transportation.class)))
                .thenReturn(new TransportationResponse());
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
    void findRoutes_DirectFlight_ShouldReturnOneRoute() {
        LocalDate date = LocalDate.of(2025, 3, 10);

        when(locationService.findById(anyLong())).thenReturn(new Location());
        when(transportationRepository.findBeforeCandidates(1, istanbul.getId()))
                .thenReturn(Collections.emptyList());
        when(transportationRepository.findAfterCandidates(1, london.getId()))
                .thenReturn(Collections.emptyList());
        when(transportationRepository.findFlightCandidates(
                eq(1), eq(Set.of(istanbul.getId())), eq(Set.of(london.getId()))))
                .thenReturn(List.of(flight));
        mockMapper();

        List<RouteResponse> routes = routeService.findRoutes(
                istanbul.getId(), london.getId(), date);

        assertThat(routes).hasSize(1);
        verify(transportationRepository).findFlightCandidates(
                eq(1), eq(Set.of(istanbul.getId())), eq(Set.of(london.getId())));
    }

    @Test
    void findRoutes_WithBeforeTransfer_ShouldReturnOneRoute() {
        LocalDate date = LocalDate.of(2025, 3, 10);

        when(locationService.findById(anyLong())).thenReturn(new Location());
        when(transportationRepository.findBeforeCandidates(1, taksim.getId()))
                .thenReturn(List.of(busToAirport));
        when(transportationRepository.findAfterCandidates(1, london.getId()))
                .thenReturn(Collections.emptyList());
        when(transportationRepository.findFlightCandidates(
                eq(1),
                eq(Set.of(taksim.getId(), istanbul.getId())),
                eq(Set.of(london.getId()))))
                .thenReturn(List.of(flight));
        mockMapper();

        List<RouteResponse> routes = routeService.findRoutes(
                taksim.getId(), london.getId(), date);

        assertThat(routes).hasSize(1);
    }

    @Test
    void findRoutes_WithAfterTransfer_ShouldReturnOneRoute() {
        LocalDate date = LocalDate.of(2025, 3, 10);

        when(locationService.findById(anyLong())).thenReturn(new Location());
        when(transportationRepository.findBeforeCandidates(1, istanbul.getId()))
                .thenReturn(Collections.emptyList());
        when(transportationRepository.findAfterCandidates(1, wembley.getId()))
                .thenReturn(List.of(busFromAirport));
        when(transportationRepository.findFlightCandidates(
                eq(1),
                eq(Set.of(istanbul.getId())),
                eq(Set.of(london.getId(), wembley.getId()))))
                .thenReturn(List.of(flight));
        mockMapper();

        List<RouteResponse> routes = routeService.findRoutes(
                istanbul.getId(), wembley.getId(), date);

        assertThat(routes).hasSize(1);
    }

    @Test
    void findRoutes_WithBeforeAndAfterTransfer_ShouldReturnAllCombinations() {
        LocalDate date = LocalDate.of(2025, 3, 10);

        when(locationService.findById(anyLong())).thenReturn(new Location());
        when(transportationRepository.findBeforeCandidates(1, taksim.getId()))
                .thenReturn(List.of(busToAirport, uberToAirport));
        when(transportationRepository.findAfterCandidates(1, wembley.getId()))
                .thenReturn(List.of(busFromAirport, uberFromAirport));
        when(transportationRepository.findFlightCandidates(
                eq(1),
                eq(Set.of(taksim.getId(), istanbul.getId())),
                eq(Set.of(london.getId(), wembley.getId()))))
                .thenReturn(List.of(flight));
        mockMapper();

        List<RouteResponse> routes = routeService.findRoutes(
                taksim.getId(), wembley.getId(), date);

        assertThat(routes).hasSize(4);
    }

    @Test
    void findRoutes_NoFlights_ShouldReturnEmpty() {
        LocalDate date = LocalDate.of(2025, 3, 10);

        when(locationService.findById(anyLong())).thenReturn(new Location());
        when(transportationRepository.findBeforeCandidates(anyInt(), anyLong()))
                .thenReturn(Collections.emptyList());
        when(transportationRepository.findAfterCandidates(anyInt(), anyLong()))
                .thenReturn(Collections.emptyList());
        when(transportationRepository.findFlightCandidates(anyInt(), anySet(), anySet()))
                .thenReturn(Collections.emptyList());

        List<RouteResponse> routes = routeService.findRoutes(
                istanbul.getId(), london.getId(), date);

        assertThat(routes).isEmpty();
        verifyNoInteractions(transportationMapper);
    }

    @Test
    void findRoutes_FlightOriginIdSetIncludesBeforeDestinations() {
        LocalDate date = LocalDate.of(2025, 3, 10);

        when(locationService.findById(anyLong())).thenReturn(new Location());
        when(transportationRepository.findBeforeCandidates(1, taksim.getId()))
                .thenReturn(List.of(busToAirport));
        when(transportationRepository.findAfterCandidates(1, london.getId()))
                .thenReturn(Collections.emptyList());

        Set<Long> expectedOriginIds = Set.of(istanbul.getId(), taksim.getId());
        Set<Long> expectedDestIds = Set.of(london.getId());

        when(transportationRepository.findFlightCandidates(
                eq(1), eq(expectedOriginIds), eq(expectedDestIds)))
                .thenReturn(List.of(flight));
        mockMapper();

        List<RouteResponse> routes = routeService.findRoutes(
                taksim.getId(), london.getId(), date);

        assertThat(routes).hasSize(1);
        verify(transportationRepository).findFlightCandidates(
                eq(1), eq(expectedOriginIds), eq(expectedDestIds));
    }
}