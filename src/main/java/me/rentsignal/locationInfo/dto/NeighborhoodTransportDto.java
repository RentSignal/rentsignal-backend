package me.rentsignal.locationInfo.dto;

import java.util.List;

public record NeighborhoodTransportDto(
        String name,
        List<SubwayStationDto> subwayStations,
        List<TransportCountDto> counts
) {

    public record TransportCountDto(
            String transportType,
            int count,
            int ratioToAverage
    ) {}

}
