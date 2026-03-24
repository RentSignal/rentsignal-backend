package me.rentsignal.recommendation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.rentsignal.global.response.BaseResponse;
import me.rentsignal.global.security.CustomPrincipal;
import me.rentsignal.recommendation.dto.RecommendRequestDto;
import me.rentsignal.recommendation.dto.RecommendResponseDto;
import me.rentsignal.recommendation.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping
    public ResponseEntity<BaseResponse<?>> getRecommendation(@AuthenticationPrincipal CustomPrincipal customPrincipal,
                                                             @Valid @RequestBody RecommendRequestDto recommendRequestDto) {
        RecommendResponseDto recommendation = recommendationService.getRecommendation(customPrincipal.getId(), recommendRequestDto);
        return ResponseEntity
                .ok(BaseResponse.success(recommendation));
    }

}
