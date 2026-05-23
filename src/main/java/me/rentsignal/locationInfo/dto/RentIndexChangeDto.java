package me.rentsignal.locationInfo.dto;

import java.util.List;

public record RentIndexChangeDto(
        List<RankItemDto> rise,
        List<RankItemDto> fall
) {
}
