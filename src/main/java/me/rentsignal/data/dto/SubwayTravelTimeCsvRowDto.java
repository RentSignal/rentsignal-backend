package me.rentsignal.data.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubwayTravelTimeCsvRowDto {

    private String lineName;

    private String stationName;

    private Integer travelTimeSeconds;

}
