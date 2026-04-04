package me.rentsignal.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class IndexApiResponseDto {

    @JsonProperty("SttsApiTblData")
    private List<SttsApiTblData> sttsApiTblData;

    @Data
    public static class SttsApiTblData {

        private List<Map<String, Object>> head;

        private List<Row> row;

    }

    @Data
    public static class Row {

        // areaName (도심권, 동북권 ..)
        @JsonProperty("CLS_NM")
        private String areaGroup;

        // (서울>강북지역>도심권)
        @JsonProperty("CLS_FULLNM")
        private String areaName;

        // 값
        @JsonProperty("DTA_VAL")
        private BigDecimal value;

        // 시점 (YYYYMM)
        @JsonProperty("WRTTIME_IDTFR_ID")
        private String date;

    }

}
