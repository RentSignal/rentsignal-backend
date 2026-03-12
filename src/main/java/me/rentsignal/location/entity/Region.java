package me.rentsignal.location.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.rentsignal.global.entity.BaseTimeEntity;

/**
 * 권역 (ex. 강남 서남권, 강북 도심권)
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"province_id", "area_group", "area_name"})}
)
public class Region extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    // 강남, 강북
    @Column(nullable = false, name = "area_group")
    private String areaGroup;

    // 도심권, 서남권, 서북권, 동남권, 동북권
    @Column(nullable = false, name = "area_name")
    private String areaName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id", nullable = false)
    private Province province;

}
