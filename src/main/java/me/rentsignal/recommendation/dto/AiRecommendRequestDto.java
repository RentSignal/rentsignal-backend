package me.rentsignal.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiRecommendRequestDto {

    private PrioritiesDto priorities;

    // score = 편의시설 점수 순, value = 가성비순
    @JsonProperty("sort_by")
    private String sortBy;

    // 1 = 오피스텔/전세, 2 = 오피스텔/월세, 3 = 원룸/전세, 4 = 원룸/월세
    @Min(1) @Max(4)
    @JsonProperty("housing_type")
    private int housingType;

    @JsonProperty("user_dong")
    private String userDong;

    @JsonProperty("radius_km")
    @Builder.Default
    private Double radiusKm = 7.0;


    // 카테고리별 순위 (1 ~ 5)
    @Data
    @Builder
    public static class PrioritiesDto {

        @Min(1) @Max(5)
        @JsonProperty("convenience_store")
        private int convenienceStore;

        @Min(1) @Max(5)
        private int cafe;

        @Min(1) @Max(5)
        private int hospital;

        @Min(1) @Max(5)
        private int pharmacy;

        @Min(1) @Max(5)
        private int restaurant;

        @Min(1) @Max(5)
        @JsonProperty("large_mart")
        private int largeMart;

        @Min(1) @Max(5)
        private int transport;

        @Min(1) @Max(5)
        private int safety;

    }

}
