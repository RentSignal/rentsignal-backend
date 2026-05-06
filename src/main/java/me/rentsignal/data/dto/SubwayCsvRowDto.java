package me.rentsignal.data.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubwayCsvRowDto {

    private String name;

    private String line;

    private Double latitude;

    private Double longitude;

}
