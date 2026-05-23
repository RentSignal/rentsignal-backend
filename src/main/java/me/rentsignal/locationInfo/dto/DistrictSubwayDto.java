package me.rentsignal.locationInfo.dto;

import java.math.BigDecimal;
import java.util.List;

public record DistrictSubwayDto(
        String name,
        BigDecimal value,
        List<SubwayStationDto> subwayStations
) {

    public record SubwayStationDto(
            String lineName,
            String stationName
    ) {}

}
