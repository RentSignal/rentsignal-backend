package me.rentsignal.data.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SafetyRowDto {

    private String districtName;

    private int count;

}
