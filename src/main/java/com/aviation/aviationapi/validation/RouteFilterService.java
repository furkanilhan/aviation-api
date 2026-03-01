package com.aviation.aviationapi.validation;

import com.aviation.aviationapi.model.entity.Transportation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteFilterService {

    private final List<RouteFilterStrategy> filters;

    public boolean isValidRoute(Transportation before, Transportation flight, Transportation after) {
        return filters.stream()
                .allMatch(filter -> filter.isSatisfiedBy(before, flight, after));
    }
}
