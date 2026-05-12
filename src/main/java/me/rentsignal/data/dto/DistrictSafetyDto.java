package me.rentsignal.data.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DistrictSafetyDto {

    private String districtName;

    private int cctvCount;

    private int crimeCount;

}
