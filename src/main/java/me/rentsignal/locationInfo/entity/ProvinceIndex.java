package me.rentsignal.locationInfo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.rentsignal.global.entity.BaseTimeEntity;
import me.rentsignal.location.entity.Province;

import java.math.BigDecimal;

/**
 * 시/도 기준 지수 (소비자 심리 지수)
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"province_id", "base_year_month"})}
)
public class ProvinceIndex extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id", nullable = false)
    private Province province;

    @Column(nullable = false)
    private BigDecimal consumerSentimentIndex;

    // YYYYMM
    @Pattern(regexp = "\\d{6}")
    @Column(nullable = false, name = "base_year_month", length = 6)
    private String baseYearMonth;

    @Builder
    public ProvinceIndex(Province province, BigDecimal consumerSentimentIndex, String baseYearMonth) {
        this.province = province;
        this.consumerSentimentIndex = consumerSentimentIndex;
        this.baseYearMonth = baseYearMonth;
    }

}
