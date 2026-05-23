package me.rentsignal.locationInfo.dto;

import java.math.BigDecimal;
import java.util.List;

public record SubwayAccessibilityIndexDto(
        List<IndexItemDto> high,
        List<IndexItemDto> changeRate,
        List<DistrictIndexDto> districtIndexes
) {
    public record DistrictIndexDto(
            Long id,
            String name,
            BigDecimal value
    ) {}
}
