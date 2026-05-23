package me.rentsignal.locationInfo.dto;

import java.util.List;

public record ConvenienceRankDto(
        List<NeighborhoodConvenienceCountDto> ranking
) {

    public record NeighborhoodConvenienceCountDto(
            int rank,
            Long id,
            String name,
            Long count
    ) {
    }

}
