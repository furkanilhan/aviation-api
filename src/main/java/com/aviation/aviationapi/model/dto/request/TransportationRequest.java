package com.aviation.aviationapi.model.dto.request;

import com.aviation.aviationapi.model.enums.TransportationType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportationRequest {

    @NotNull(message = "Origin location ID is required")
    private Long originLocationId;

    @NotNull(message = "Destination location ID is required")
    private Long destinationLocationId;

    @NotNull(message = "Transportation type is required")
    private TransportationType transportationType;

    @NotNull(message = "Operating days is required")
    @Size(min = 1, max = 7, message = "Operating days must have between 1 and 7 days")
    private List<Integer> operatingDays;
}
