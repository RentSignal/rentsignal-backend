package me.rentsignal.locationInfo.dto;

import java.math.BigDecimal;

public record RentIndexItemDto(
        int rank,
        String regionName,
        BigDecimal value
) {
}
