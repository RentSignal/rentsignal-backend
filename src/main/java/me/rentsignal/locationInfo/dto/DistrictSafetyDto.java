package me.rentsignal.locationInfo.dto;

import java.math.BigDecimal;
import java.util.List;

public record DistrictSafetyDto(
        List<RankItemDto> ranking,
        List<DistrictSafetyScoreDto> districtSafetyScores
) {

    public record DistrictSafetyScoreDto(
            String name,
            BigDecimal value
    ) {}

}
