package me.rentsignal.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ConvenienceStoreApiResponseDto {

    private Body body;

    @Data
    public static class Body {

        private Items items;

    }

    @Data
    public static class Items {

        private List<Item> item;

    }

    @Data
    public static class Item {

        @JsonProperty("fclty_nm")
        private String facilityName;

        @JsonProperty("adres")
        private String address;

        @JsonProperty("x")
        private Double x;

        @JsonProperty("y")
        private Double y;

    }

}
