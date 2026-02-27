package com.aviation.aviationapi.service;

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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private Transportation flight, busToAirport, busFromAirport, uberFromAirport;

    @BeforeEach
    void setUp() {
        taksim = Location.builder().id(1L).name("Taksim").locationCode("TKSM").build();
        istanbul = Location.builder().id(2L).name("Istanbul Airport").locationCode("IST").build();
        london = Location.builder().id(3L).name("London Heathrow").locationCode("LHR").build();
        wembley = Location.builder().id(4L).name("Wembley").locationCode("CCWEM").build();

        flight = Transportation.builder()
                .id(1L)
                .originLocation(istanbul)
                .destinationLocation(london)
                .transportationType(TransportationType.FLIGHT)
                .operatingDays(List.of(1, 2, 3, 4, 5, 6, 7))
                .build();

        busToAirport = Transportation.builder()
                .id(2L)
                .originLocation(taksim)
                .destinationLocation(istanbul)
                .transportationType(TransportationType.BUS)
                .operatingDays(List.of(1, 2, 3, 4, 5, 6, 7))
                .build();

        busFromAirport = Transportation.builder()
                .id(3L)
                .originLocation(london)
                .destinationLocation(wembley)
                .transportationType(TransportationType.BUS)
                .operatingDays(List.of(1, 2, 3, 4, 5, 6, 7))
                .build();

        uberFromAirport = Transportation.builder()
                .id(4L)
                .originLocation(london)
                .destinationLocation(wembley)
                .transportationType(TransportationType.UBER)
                .operatingDays(List.of(1, 2, 3, 4, 5, 6, 7))
                .build();
    }

    @Test
    void findRoutes_DirectFlight_ShouldReturnOneRoute() {
        when(transportationRepository.findByOperatingDayWithLocations(1))
                .thenReturn(List.of(flight));
        when(transportationMapper.toResponse(any())).thenReturn(new TransportationResponse());

        List<RouteResponse> routes = routeService.findRoutes(2L, 3L, LocalDate.of(2025, 3, 10));

        assertThat(routes).hasSize(1);
        verify(transportationRepository).findByOperatingDayWithLocations(1);
    }

    @Test
    void findRoutes_WithBeforeAndAfterTransfer_ShouldReturnAllCombinations() {
        when(transportationRepository.findByOperatingDayWithLocations(1))
                .thenReturn(List.of(flight, busToAirport, busFromAirport, uberFromAirport));
        when(transportationMapper.toResponse(any())).thenReturn(new TransportationResponse());

        // Taksim -> IST -> LHR -> Wembley
        List<RouteResponse> routes = routeService.findRoutes(1L, 4L, LocalDate.of(2025, 3, 10));

        // before(1) x after(2) = 2 rota bekliyoruz
        assertThat(routes).hasSize(2);
    }

    @Test
    void findRoutes_NoActiveTransportations_ShouldReturnEmpty() {
        when(transportationRepository.findByOperatingDayWithLocations(7))
                .thenReturn(List.of());

        // Pazar günü hiç transport yok
        List<RouteResponse> routes = routeService.findRoutes(1L, 4L, LocalDate.of(2025, 3, 9));

        assertThat(routes).isEmpty();
    }

    @Test
    void findRoutes_OnlyBeforeFlight_ShouldReturnRoute() {
        when(transportationRepository.findByOperatingDayWithLocations(1))
                .thenReturn(List.of(flight, busToAirport));
        when(transportationMapper.toResponse(any())).thenReturn(new TransportationResponse());

        // Taksim -> IST -> LHR (after flight yok)
        List<RouteResponse> routes = routeService.findRoutes(1L, 3L, LocalDate.of(2025, 3, 10));

        assertThat(routes).hasSize(1);
        assertThat(routes.get(0).getAfterFlight()).isNull();
    }

    @Test
    void findRoutes_OnlyAfterFlight_ShouldReturnRoutes() {
        when(transportationRepository.findByOperatingDayWithLocations(1))
                .thenReturn(List.of(flight, busFromAirport, uberFromAirport));
        when(transportationMapper.toResponse(any())).thenReturn(new TransportationResponse());

        // IST -> LHR -> Wembley (before flight yok)
        List<RouteResponse> routes = routeService.findRoutes(2L, 4L, LocalDate.of(2025, 3, 10));

        assertThat(routes).hasSize(2);
        assertThat(routes.get(0).getBeforeFlight()).isNull();
    }
}