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
 * 시 / 도
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Province extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    @OneToMany(mappedBy = "province")
    private List<District> districts = new ArrayList<>();

    @Builder
    public Province(String name, String code) {
        this.name = name;
        this.code = code;
    }

}
