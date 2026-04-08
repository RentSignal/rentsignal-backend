package me.rentsignal.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SentimentIndexApiResponseDto {

    @JsonProperty("PRD_DE")
    private String date;

    @JsonProperty("C1_NM")
    private String provinceName;

    @JsonProperty("DT")
    private BigDecimal value;

}
