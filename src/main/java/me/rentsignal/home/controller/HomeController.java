package me.rentsignal.home.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.rentsignal.community.dto.PostListItemResponse;
import me.rentsignal.global.response.BaseResponse;
import me.rentsignal.global.security.CustomPrincipal;
import me.rentsignal.home.service.HomeService;
import me.rentsignal.recommendation.dto.RecommendRequestDto;
import me.rentsignal.recommendation.dto.RecommendResponseDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/reviews")
    public BaseResponse<List<PostListItemResponse>> getHomeReviews() {
        return BaseResponse.success(homeService.getHomeReviews());
    }

    @PostMapping("/recommendations")
    public BaseResponse<List<RecommendResponseDto.RecommendationDto>> getHomeRecommendations(
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            @Valid @RequestBody RecommendRequestDto requestDto
    ) {
        return BaseResponse.success(
                homeService.getHomeRecommendations(customPrincipal.getId(), requestDto)
        );
    }
}