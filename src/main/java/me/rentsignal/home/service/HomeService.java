package me.rentsignal.home.service;

import lombok.RequiredArgsConstructor;
import me.rentsignal.community.dto.PostListItemResponse;
import me.rentsignal.community.repository.PostRepository;
import me.rentsignal.locationInfo.dto.RankItemDto;
import me.rentsignal.locationInfo.entity.DistrictIndex;
import me.rentsignal.locationInfo.repository.DistrictIndexRepository;
import me.rentsignal.recommendation.dto.RecommendRequestDto;
import me.rentsignal.recommendation.dto.RecommendResponseDto;
import me.rentsignal.recommendation.service.RecommendationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private final PostRepository postRepository;
    private final RecommendationService recommendationService;
    private final DistrictIndexRepository districtIndexRepository;

    public List<PostListItemResponse> getHomeReviews() {
        return postRepository.findTop5ByIsDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .map(PostListItemResponse::from)
                .toList();
    }

    public List<RecommendResponseDto.RecommendationDto> getTodayRecommendations() {
        RecommendRequestDto requestDto = createRandomRecommendRequest();

        return recommendationService.getRecommendationForHome(requestDto)
                .getRecommendedNeighborhoods();
    }

    public List<RankItemDto> getSubwayAccessibilityRanking() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");

        String baseYearMonth = YearMonth.now()
                .minusMonths(2)
                .format(formatter);

        List<DistrictIndex> indexes = districtIndexRepository
                .findByDistrict_Province_NameAndBaseYearMonthOrderBySubwayAccessibilityIndexDesc(
                        "서울특별시",
                        baseYearMonth
                );

        List<RankItemDto> ranking = new ArrayList<>();

        for (int i = 0; i < Math.min(5, indexes.size()); i++) {
            DistrictIndex index = indexes.get(i);

            ranking.add(new RankItemDto(
                    i + 1,
                    index.getDistrict().getName(),
                    index.getSubwayAccessibilityIndex().setScale(1, RoundingMode.HALF_UP)
            ));
        }

        return ranking;
    }

    private RecommendRequestDto createRandomRecommendRequest() {
        RecommendRequestDto requestDto = new RecommendRequestDto();

        requestDto.setUserDong("");
        requestDto.setHouseType(randomOne("오피스텔", "원룸"));
        requestDto.setRentType(randomOne("전세", "월세"));
        requestDto.setSortBy(randomOne("가성비", "편의시설"));

        if ("편의시설".equals(requestDto.getSortBy())) {
            List<String> facilities = new ArrayList<>(
                    List.of("편의점", "카페", "병원", "약국", "음식점", "대형마트", "교통", "치안")
            );

            Collections.shuffle(facilities);

            requestDto.setFacilityPriorities(facilities.subList(0, 5));
        }

        return requestDto;
    }

    private String randomOne(String... values) {
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }
}