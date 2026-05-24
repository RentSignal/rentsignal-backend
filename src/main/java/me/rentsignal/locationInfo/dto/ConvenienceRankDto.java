package me.rentsignal.locationInfo.dto;

import java.util.List;

public record ConvenienceRankDto(
        List<NeighborhoodConvenienceRankDto> ranking
) {

    public record NeighborhoodConvenienceRankDto(
            int rank,
            Long id,
            String name,
            Long count
    ) {
    }

}
