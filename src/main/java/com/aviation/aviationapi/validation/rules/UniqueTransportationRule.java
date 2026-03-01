package com.aviation.aviationapi.validation.rules;

import com.aviation.aviationapi.model.entity.Transportation;
import com.aviation.aviationapi.validation.RouteFilterStrategy;
import org.springframework.stereotype.Component;

@Component
public class UniqueTransportationRule implements RouteFilterStrategy {

    @Override
    public boolean isSatisfiedBy(Transportation before, Transportation flight, Transportation after) {
        if (before != null && after != null) {
            return !before.getId().equals(after.getId());
        }
        return true;
    }
}
