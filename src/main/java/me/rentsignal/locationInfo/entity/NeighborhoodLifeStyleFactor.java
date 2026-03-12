package me.rentsignal.locationInfo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.rentsignal.global.entity.BaseTimeEntity;
import me.rentsignal.location.entity.Neighborhood;

import java.math.BigDecimal;

/**
 * 읍/면/동 기준 생활요소 (치안, 비용)
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class NeighborhoodLifeStyleFactor extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "neighborhood_id", nullable = false)
    private Neighborhood neighborhood;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FactorType factorType;

    // factorType이 COST가 아닌 경우 Null
    @Enumerated(EnumType.STRING)
    private LeaseType leaseType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

}
