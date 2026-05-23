package me.rentsignal.locationInfo.dto;

import java.math.BigDecimal;

public record RankItemDto(
        int rank,
        String name,
        BigDecimal value
) {
}
