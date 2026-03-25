package me.rentsignal.recommendation.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecommendResponseDto {

    private String houseType;

    private String rentType;

    private String priority;

    private List<RecommendationDto> recommendedNeighborhoods;

    @Data
    @Builder
    public static class RecommendationDto {

        private String dongName;

        private int rank;

        private Double score;

        private Double distance;

        private FacilityDto facilityCount;

        private Integer avgDeposit;

        // 월세일 때만 포함
        private Integer avgMonthly;

        private Long transport;

        private Double safety;

    }

    @Data
    @Builder
    public static class FacilityDto {

        private AiRecommendResponseDto.FacilityInfoDto restaurant;

        private AiRecommendResponseDto.FacilityInfoDto hospital;

        private AiRecommendResponseDto.FacilityInfoDto cafe;

        private AiRecommendResponseDto.FacilityInfoDto pharmacy;

        private AiRecommendResponseDto.FacilityInfoDto mart;

    }

}
