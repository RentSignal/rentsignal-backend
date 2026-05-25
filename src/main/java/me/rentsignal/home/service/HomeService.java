package me.rentsignal.home.service;

import lombok.RequiredArgsConstructor;
import me.rentsignal.community.dto.PostListItemResponse;
import me.rentsignal.community.repository.PostRepository;
import me.rentsignal.recommendation.dto.RecommendRequestDto;
import me.rentsignal.recommendation.dto.RecommendResponseDto;
import me.rentsignal.recommendation.service.RecommendationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private final PostRepository postRepository;
    private final RecommendationService recommendationService;

    public List<PostListItemResponse> getHomeReviews() {
        return postRepository.findTop5ByIsDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .map(PostListItemResponse::from)
                .toList();
    }

    public List<RecommendResponseDto.RecommendationDto> getHomeRecommendations(
            Long userId,
            RecommendRequestDto requestDto
    ) {
        return recommendationService.getRecommendation(userId, requestDto)
                .getRecommendedNeighborhoods();
    }
}
