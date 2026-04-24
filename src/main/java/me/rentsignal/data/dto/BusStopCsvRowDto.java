package me.rentsignal.data.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BusStopCsvRowDto {

    private String name;

    private Double latitude;

    private Double longitude;

}
