package com.aviation.aviationapi.model.dto.request;

import com.aviation.aviationapi.model.enums.TransportationType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Set;

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
    private Set<@Min(1) @Max(7) Integer> operatingDays;

    @AssertTrue(message = "Origin and destination cannot be the same")
    public boolean isRouteValid() {
        if (originLocationId == null || destinationLocationId == null) return true;
        return !originLocationId.equals(destinationLocationId);
    }
}
