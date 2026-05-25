package me.rentsignal.locationInfo.dto;

import java.util.List;

public record RecommendedNeighborhoodByBusinessDistrict(
        Long id,
        String name,
        List<SubwayReachableStationDto> stations
) {
}
