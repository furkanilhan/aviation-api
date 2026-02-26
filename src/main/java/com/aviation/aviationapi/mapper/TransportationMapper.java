package com.aviation.aviationapi.mapper;

import com.aviation.aviationapi.model.dto.response.TransportationResponse;
import com.aviation.aviationapi.model.entity.Transportation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {LocationMapper.class})
public interface TransportationMapper {

    @Mapping(source = "originLocation", target = "originLocation")
    @Mapping(source = "destinationLocation", target = "destinationLocation")
    TransportationResponse toResponse(Transportation transportation);

    List<TransportationResponse> toResponseList(List<Transportation> transportations);
}
