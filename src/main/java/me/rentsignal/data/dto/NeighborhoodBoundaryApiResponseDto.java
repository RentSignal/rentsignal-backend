package me.rentsignal.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class NeighborhoodBoundaryApiResponseDto {

    private ResponseDto response;

    @Data
    public static class ResponseDto {
        private PageDto page;
        private ResultDto result;
    }

    @Data
    public static class PageDto {
        private int total;
    }

    @Data
    public static class ResultDto {
        private FeatureCollectionDto featureCollection;
    }

    @Data
    public static class FeatureCollectionDto {
        private List<FeatureDto> features;
    }

    @Data
    public static class FeatureDto {
        private GeometryDto geometry;
        private PropertiesDto properties;
    }

    @Data
    public static class GeometryDto {
        private String type;
        private List<List<List<List<Double>>>> coordinates;
    }

    @Data
    public static class PropertiesDto {

        @JsonProperty("emd_cd")
        private String emdCode;

        @JsonProperty("full_nm")
        private String fullName;

    }

}
