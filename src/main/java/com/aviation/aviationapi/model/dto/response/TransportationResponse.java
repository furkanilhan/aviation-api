package com.aviation.aviationapi.model.dto.response;

import com.aviation.aviationapi.model.enums.TransportationType;
import lombok.*;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportationResponse implements Serializable {
    private Long id;
    private LocationResponse originLocation;
    private LocationResponse destinationLocation;
    private TransportationType transportationType;
    private Set<Integer> operatingDays;
}

