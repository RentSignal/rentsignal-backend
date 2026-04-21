package me.rentsignal.locationInfo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.rentsignal.global.entity.BaseTimeEntity;
import me.rentsignal.location.entity.Neighborhood;

import java.math.BigDecimal;

/**
 * 읍/면/동 기준 생활요소 (편의시설)
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"neighborhood_id", "name"}
        )
)
public class NeighborhoodConvenience extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "neighborhood_id", nullable = false)
    private Neighborhood neighborhood;

    // 병원, 편의점, 카페, 마트 등
    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Builder
    public NeighborhoodConvenience(Neighborhood neighborhood, String type, String name, Double latitude, Double longitude) {
        this.neighborhood = neighborhood;
        this.type = type;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
