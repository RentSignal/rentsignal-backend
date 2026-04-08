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
 * 권역 (ex. 강남 서남권, 강북 도심권)
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
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

    @OneToMany(mappedBy = "region")
    private List<District> districts = new ArrayList<>();

    @Builder
    public Region(String areaGroup, String areaName) {
        this.areaGroup = areaGroup;
        this.areaName = areaName;
    }

    public void addDistricts(List<District> districts) {
        for (District district : districts) {
            if (!this.districts.contains(district)) {
                this.districts.add(district);
            }
            district.assignRegion(this);
        }
    }

}
