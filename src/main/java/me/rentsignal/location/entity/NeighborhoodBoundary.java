package me.rentsignal.location.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.rentsignal.global.entity.BaseTimeEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class NeighborhoodBoundary extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "neighborhood_id", nullable = false, unique = true)
    private Neighborhood neighborhood;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String geometryJson;

    @Builder
    public NeighborhoodBoundary(Neighborhood neighborhood, String geometryJson) {
        this.neighborhood = neighborhood;
        this.geometryJson = geometryJson;
    }

}
