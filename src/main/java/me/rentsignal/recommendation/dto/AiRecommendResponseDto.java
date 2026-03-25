package me.rentsignal.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AiRecommendResponseDto {

    private List<AiRecommendationDto> results;

    @Data
    public static class AiRecommendationDto {

        private int rank;

        @JsonProperty("dong_name")
        private String dongName;

        @JsonProperty("distance_km")
        private Double distanceKm;

        private Double score;

        private CategoryDto categories;

        private PriceDto price;

    }

    @Data
    public static class CategoryDto {

        @JsonProperty("편의점")
        private FacilityInfoDto convenienceStore;

        @JsonProperty("카페")
        private FacilityInfoDto cafe;

        @JsonProperty("병원")
        private FacilityInfoDto hospital;

        @JsonProperty("약국")
        private FacilityInfoDto pharmacy;

        @JsonProperty("음식점")
        private FacilityInfoDto restaurant;

        @JsonProperty("대형마트")
        private FacilityInfoDto mart;

        @JsonProperty("교통")
        private Map<String, Object> transport;

        @JsonProperty("치안")
        private Map<String, Object> safety;

    }

    @Data
    public static class PriceDto {

        @JsonProperty("avg_deposit")
        private Integer avgDeposit;

        // 월세일 때만 포함
        @JsonProperty("avg_monthly")
        private Integer avgMonthly;

        // 월세일 때만 포함
        @JsonProperty("monthly_cost")
        private Integer monthlyCost;

        private Integer deals;

    }

    @Data
    public static class FacilityInfoDto {

        private Long count;

        private Double normalized;

    }

}
