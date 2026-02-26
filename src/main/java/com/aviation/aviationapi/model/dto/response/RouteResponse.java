package com.aviation.aviationapi.model.dto.response;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteResponse implements Serializable {
    private TransportationResponse beforeFlight;
    private TransportationResponse flight;
    private TransportationResponse afterFlight;
}
