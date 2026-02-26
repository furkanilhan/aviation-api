package com.aviation.aviationapi.mapper;

import com.aviation.aviationapi.model.dto.request.LocationRequest;
import com.aviation.aviationapi.model.dto.response.LocationResponse;
import com.aviation.aviationapi.model.entity.Location;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationResponse toResponse(Location location);
    List<LocationResponse> toResponseList(List<Location> locations);
    Location toEntity(LocationRequest request);
}

