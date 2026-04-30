package me.rentsignal.locationInfo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.rentsignal.location.entity.Neighborhood;

import java.math.BigDecimal;

/**
 * 읍/면/동 기준 월세/전세
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class NeighborhoodLease {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "neighborhood_id", nullable = false)
    private Neighborhood neighborhood;

    @Enumerated(EnumType.STRING)
    private LeaseType leaseType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal deposit;

    // LeaseType == JEONSE인 경우 null
    @Column(precision = 10, scale = 2)
    private BigDecimal monthlyRent;

    // 면적 (m2)
    @Column(precision = 10, scale = 2)
    private BigDecimal areaM2;

}
