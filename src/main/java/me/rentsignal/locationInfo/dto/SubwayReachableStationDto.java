package me.rentsignal.locationInfo.dto;

public record SubwayReachableStationDto(
        String lineName,
        String stationName,
        int travelTimeMinutes,
        int travelTimeSeconds
) {
}
