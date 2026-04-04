package me.rentsignal.locationInfo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.rentsignal.global.entity.BaseTimeEntity;
import me.rentsignal.location.entity.Region;

import java.math.BigDecimal;

/**
 * 권역 기준 지수 (전월세 통합 지수)
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"region_id", "housing_type", "base_year_month"})}
)
public class RegionIndex extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Enumerated(EnumType.STRING)
    @Column(name = "housing_type", nullable = false)
    private HousingType housingType;

    @Column(nullable = false, scale = 2)
    private BigDecimal rentCompositeIndex;

    // YYYYMM
    @Pattern(regexp = "\\d{6}")
    @Column(nullable = false, name = "base_year_month", length = 6)
    private String baseYearMonth;

    @Builder
    public RegionIndex(Region region, HousingType housingType, BigDecimal rentCompositeIndex, String baseYearMonth) {
        this.region = region;
        this.housingType = housingType;
        this.rentCompositeIndex = rentCompositeIndex;
        this.baseYearMonth = baseYearMonth;
    }

}
