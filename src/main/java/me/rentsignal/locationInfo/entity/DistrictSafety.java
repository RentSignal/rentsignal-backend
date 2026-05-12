package me.rentsignal.locationInfo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.rentsignal.global.entity.BaseTimeEntity;
import me.rentsignal.location.entity.District;

/**
 * 시/군/구 기준 치안
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DistrictSafety extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false, unique = true)
    private District district;

    @Column(nullable = false)
    private int cctvCount;

    @Column(nullable = false)
    private int crimeCount;

    @Column(nullable = false)
    private double safetyScore;

    @Builder
    public DistrictSafety(District district, int cctvCount, int crimeCount, double safetyScore) {
        this.district = district;
        this.cctvCount = cctvCount;
        this.crimeCount = crimeCount;
        this.safetyScore = safetyScore;
    }

    public void update(int cctvCount, int crimeCount, double safetyScore) {
        this.cctvCount = cctvCount;
        this.crimeCount = crimeCount;
        this.safetyScore = safetyScore;
    }

}
