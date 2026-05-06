package me.rentsignal.data.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConvenienceRowDto {

    private String code;

    private String name;

    private String type;

    private Double latitude;

    private Double longitude;

}
