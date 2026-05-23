package me.rentsignal.locationInfo.dto;

import java.math.BigDecimal;

public record IndexItemDto(
        int rank,
        String name,
        BigDecimal value
) {
}
