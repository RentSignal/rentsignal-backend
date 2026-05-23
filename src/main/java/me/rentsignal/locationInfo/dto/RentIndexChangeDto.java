package me.rentsignal.locationInfo.dto;

import java.util.List;

public record RentIndexChangeDto(
        List<IndexItemDto> rise,
        List<IndexItemDto> fall
) {
}
