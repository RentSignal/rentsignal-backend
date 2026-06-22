package me.rentsignal.community.dto;

import lombok.Builder;
import lombok.Getter;
import me.rentsignal.location.entity.Neighborhood;

@Getter
@Builder
public class NeighborhoodSearchResponse {

    private Long neighborhoodId;
    private String neighborhoodName;
    private String districtName;

    public static NeighborhoodSearchResponse from(Neighborhood neighborhood) {
        return NeighborhoodSearchResponse.builder()
                .neighborhoodId(neighborhood.getId())
                .neighborhoodName(neighborhood.getName())
                .districtName(neighborhood.getDistrict().getName())
                .build();
    }
}