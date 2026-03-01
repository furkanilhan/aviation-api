package com.aviation.aviationapi.validation;

import com.aviation.aviationapi.model.entity.Transportation;

public interface RouteFilterStrategy {
    boolean isSatisfiedBy(Transportation before, Transportation flight, Transportation after);
}
