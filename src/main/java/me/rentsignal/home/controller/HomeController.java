package me.rentsignal.home.controller;

import lombok.RequiredArgsConstructor;
import me.rentsignal.community.dto.PostListItemResponse;
import me.rentsignal.global.response.BaseResponse;
import me.rentsignal.home.service.HomeService;
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
}