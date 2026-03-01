package com.aviation.aviationapi.model.entity;

import com.aviation.aviationapi.model.enums.TransportationType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "transportations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transportation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_location_id", nullable = false)
    private Location originLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_location_id", nullable = false)
    private Location destinationLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "transportation_type", nullable = false)
    private TransportationType transportationType;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "transportation_operating_days",
            joinColumns = @JoinColumn(name = "transportation_id")
    )
    @Column(name = "day")
    private Set<Integer> operatingDays = new HashSet<>();
}
