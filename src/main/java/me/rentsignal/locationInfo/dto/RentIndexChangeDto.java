package me.rentsignal.locationInfo.dto;

import java.util.List;

public record RentIndexChangeDto(
        List<RentIndexItemDto> rise,
        List<RentIndexItemDto> fall
) {
}
