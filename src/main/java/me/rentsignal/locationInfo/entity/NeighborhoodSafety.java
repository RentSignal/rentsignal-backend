package me.rentsignal.locationInfo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.rentsignal.global.entity.BaseTimeEntity;
import me.rentsignal.location.entity.Neighborhood;

import java.math.BigDecimal;

/**
 * 읍/면/동 기준 치안
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class NeighborhoodSafety extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "neighborhood_id", nullable = false)
    private Neighborhood neighborhood;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

}
