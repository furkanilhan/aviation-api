package com.aviation.aviationapi.model.dto.response;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationResponse implements Serializable {
    private Long id;
    private String name;
    private String country;
    private String city;
    private String locationCode;
}
