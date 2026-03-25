package me.rentsignal.recommendation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RecommendRequestDto {

    private String userDong;

    @NotBlank
    private String houseType;

    @NotBlank
    private String rentType;

    @NotBlank
    private String sortBy;

    private List<String> facilityPriorities;

}
