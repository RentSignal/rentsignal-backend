package me.rentsignal.location.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.rentsignal.global.entity.BaseTimeEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 시 / 군 / 구
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class District extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id", nullable = false)
    private Province province;

    @OneToMany(mappedBy = "district")
    private List<Neighborhood> neighborhoods = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @Builder
    public District(String name, String code, Province province) {
        this.name = name;
        this.code = code;
        this.province = province;
    }

    public void assignRegion(Region region) {
        this.region = region;
    }

}
