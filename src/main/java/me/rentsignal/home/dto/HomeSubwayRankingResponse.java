package me.rentsignal.home.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HomeSubwayRankingResponse {

    private Integer rank;
    private Long districtId;
    private String name;
    private Double value;
}
