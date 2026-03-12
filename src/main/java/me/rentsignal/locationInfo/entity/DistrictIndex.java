package me.rentsignal.locationInfo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.rentsignal.global.entity.BaseTimeEntity;
import me.rentsignal.location.entity.District;

import java.math.BigDecimal;

/**
 * 시/군/구 기준 지수 (지하철 역세권 지수)
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"district_id", "base_year_month"})}
)
public class DistrictIndex extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @Column(nullable = false)
    private BigDecimal subwayAccessibilityIndex;

    // YYYYMM
    @Pattern(regexp = "\\d{6}")
    @Column(nullable = false, name = "base_year_month", length = 6)
    private String baseYearMonth;

}
