package me.rentsignal.locationInfo.dto;

import java.util.List;

public record ConvenienceTypeCountDto (
        String name,
        ConvenienceGroupDto mart,
        ConvenienceGroupDto convenienceStore,
        ConvenienceGroupDto hospital,
        ConvenienceGroupDto cafe
) {

    // Type별 NeighborhoodConvenience
    public record ConvenienceGroupDto (
            int count,
            List<ConvenienceDto> conveniences
    ) {}

    // 개별 NeighborhoodConvenience
    public record ConvenienceDto(
            String name,
            Double latitude,
            Double longitude
    ) {}

}
