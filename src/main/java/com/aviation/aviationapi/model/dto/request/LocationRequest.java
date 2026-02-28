package com.aviation.aviationapi.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Location code is required")
    @Size(min = 3, max = 10, message = "Location code must be between 3 and 10 characters")
    private String locationCode;

    private Double latitude;
    private Double longitude;
}
