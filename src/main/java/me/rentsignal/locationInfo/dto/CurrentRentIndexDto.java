package me.rentsignal.locationInfo.dto;

import java.util.List;

public record CurrentRentIndexDto(
        List<RankItemDto> indexes
) {
}
