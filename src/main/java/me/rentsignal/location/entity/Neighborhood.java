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
 * 읍 / 면 / 동
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"name", "district_id"})}
)
public class Neighborhood extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @OneToMany(mappedBy = "neighborhood")
    private List<Ri> ris = new ArrayList<>();

    @Builder
    public Neighborhood(String name, String code, District district) {
        this.name = name;
        this.code = code;
        this.district = district;
    }

}
