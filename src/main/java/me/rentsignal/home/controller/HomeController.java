package me.rentsignal.home.controller;

import lombok.RequiredArgsConstructor;
import me.rentsignal.community.dto.PostListItemResponse;
import me.rentsignal.global.response.BaseResponse;
import me.rentsignal.home.service.HomeService;
import me.rentsignal.locationInfo.dto.RankItemDto;
import me.rentsignal.recommendation.dto.RecommendResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/today-recommendations")
    public BaseResponse<List<RecommendResponseDto.RecommendationDto>> getTodayRecommendations() {
        return BaseResponse.success(homeService.getTodayRecommendations());
    }

    @GetMapping("/subway-accessibility-ranking")
    public BaseResponse<List<RankItemDto>> getSubwayAccessibilityRanking() {
        return BaseResponse.success(homeService.getSubwayAccessibilityRanking());
    }
}