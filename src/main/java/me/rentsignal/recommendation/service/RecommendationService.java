package me.rentsignal.recommendation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
import me.rentsignal.recommendation.dto.AiRecommendRequestDto;
import me.rentsignal.recommendation.dto.AiRecommendResponseDto;
import me.rentsignal.recommendation.dto.RecommendRequestDto;
import me.rentsignal.recommendation.dto.RecommendResponseDto;
import me.rentsignal.user.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    @Value("${RECOMMENDATION_API_URL}")
    private String RECOMMENDATION_API_URL;

    private final AuthService authService;

    public RecommendResponseDto getRecommendation(Long userId, RecommendRequestDto requestDto) {
        authService.validateUserAccess(userId);

        // 1 ) AI API 요청 DTO를 생성하기 위해 변환 ----------
        String userDong = requestDto.getUserDong() != null ? requestDto.getUserDong() : "";

        // 1-1. houseType + rentType -> housing_type으로 변환
        String houseType = requestDto.getHouseType();
        String rentType = requestDto.getRentType();

        int housingType = convertToHousingType(houseType, rentType);

        // 1-2. sortBy = "편의시설"인 경우 - AI API에 전달할 편의시설 우선순위 객체 생성
        String sortBy = requestDto.getSortBy();
        String sortByForAI;
        AiRecommendRequestDto.PrioritiesDto priorities = null;

        if (sortBy.equals("편의시설")) {
            List<String> facilityPriorities = requestDto.getFacilityPriorities();

            if (facilityPriorities == null || facilityPriorities.size() != 5)
                throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "편의시설 우선순위를 1~5순위까지 설정해야합니다.");

            priorities = AiRecommendRequestDto.PrioritiesDto.builder()
                    .convenienceStore(getRank(facilityPriorities, "편의점"))
                    .cafe(getRank(facilityPriorities, "카페"))
                    .hospital(getRank(facilityPriorities, "병원"))
                    .pharmacy(getRank(facilityPriorities, "약국"))
                    .restaurant(getRank(facilityPriorities, "음식점"))
                    .largeMart(getRank(facilityPriorities, "대형마트"))
                    .transport(getRank(facilityPriorities, "교통"))
                    .safety(getRank(facilityPriorities, "치안")).build();

            // AI API에서 편의시설 점수순 = score, 가성비순 = value
            sortByForAI = "score";
        } else {
            sortByForAI = "value";
            priorities = AiRecommendRequestDto.PrioritiesDto.builder()
                    .convenienceStore(5).cafe(5)
                    .hospital(5).pharmacy(5)
                    .restaurant(5).largeMart(5)
                    .transport(5).safety(5).build();
        }


        // 2 ) AI API 요청 DTO 생성 ----------
        AiRecommendRequestDto aiRecommendRequestDto = AiRecommendRequestDto.builder()
                .priorities(priorities)
                .sortBy(sortByForAI)
                .housingType(housingType)
                .userDong(userDong).build();


        // 3 ) AI API에 요청 ----------
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AiRecommendRequestDto> requestEntity = new HttpEntity<>(aiRecommendRequestDto, headers);

        AiRecommendResponseDto aiRecommendResponseDto;

        try {
            aiRecommendResponseDto =
                    restTemplate
                            .exchange(RECOMMENDATION_API_URL, HttpMethod.POST, requestEntity, AiRecommendResponseDto.class)
                            .getBody();

            if (aiRecommendResponseDto == null)
                throw new BaseException(ErrorCode.EXTERNAL_API_ERROR, "외부 API로부터 응답을 받아오지 못했습니다.");

        } catch (ResourceAccessException e) {
            log.error("외부 API 연결 에러 - " + e.getMessage());
            throw new BaseException(ErrorCode.EXTERNAL_API_ERROR, "외부 API 연결에 실패했습니다.");
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("외부 API 에러 - " + e.getMessage());
            throw new BaseException(ErrorCode.EXTERNAL_API_ERROR, "외부 API에서 알 수 없는 오류가 발생했습니다.");
        }


        // 4 ) 응답 DTO 생성 ----------
        // 4-1. price = null인 추천 결과는 제거
        List<AiRecommendResponseDto.AiRecommendationDto> filtered = aiRecommendResponseDto.getResults().stream()
                .filter(dto -> dto.getPrice() != null)
                .toList();

        // 4-2. 제거된 추천 결과 제외하고 응답 DTO로 변환
        List<RecommendResponseDto.RecommendationDto> recommendations = IntStream.range(0, filtered.size())
                .mapToObj(i -> toRecommendationDto(filtered.get(i), i+1))
                .toList();

        return RecommendResponseDto.builder()
                .houseType(houseType)
                .rentType(rentType)
                .priority(sortBy)
                .recommendedNeighborhoods(recommendations).build();
    }


    /** 편의시설 우선순위 리스트에서 특정 편의시설의 순위 조회 */
    private int getRank(List<String> priorities, String facility) {
        int idx = priorities.indexOf(facility);
        // 우선순위에 없는 편의시설은 5순위로 처리
        return idx == -1 ? 5 : idx + 1;
    }


    /** 프론트로부터 받은 houseType과 rentType을 AI API에 요청하기 위해 조합 */
    private int convertToHousingType(String houseType, String rentType) {
        int housing_type;
        if ("오피스텔".equals(houseType) && "전세".equals(rentType))
            housing_type = 1;
        else if ("오피스텔".equals(houseType) && "월세".equals(rentType))
            housing_type = 2;
        else if ("원룸".equals(houseType) && "전세".equals(rentType))
            housing_type = 3;
        else if ("원룸".equals(houseType) && "월세".equals(rentType))
            housing_type = 4;
        else
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "잘못된 houseType 또는 rentType입니다. houseType - " + houseType + ", rentType - " + rentType);

        return housing_type;
    }


    /** AiRecommendationDto (AI API 추천 동네 DTO)
     * -> RecommendationDto (서버에서 반환할 추천 동네 DTO) */
    private static RecommendResponseDto.RecommendationDto toRecommendationDto(AiRecommendResponseDto.AiRecommendationDto dto, int rank) {
        AiRecommendResponseDto.CategoryDto categories = dto.getCategories();
        AiRecommendResponseDto.PriceDto price = dto.getPrice();

        Map<String, Object> transport = dto.getCategories().getTransport();
        return RecommendResponseDto.RecommendationDto.builder()
                .dongName(dto.getDongName())
                .rank(rank)
                .score(dto.getScore())
                .distance(dto.getDistanceKm())
                .avgDeposit(price.getAvgDeposit())
                .avgMonthly(price.getAvgMonthly())
                .transport(Long.parseLong(transport.getOrDefault("bus", 0).toString())
                        + Long.parseLong(transport.getOrDefault("subway", 0).toString()))
                .safety(Double.parseDouble(dto.getCategories().getSafety().getOrDefault("normalized", 0).toString()))
                .facilityCount(
                        RecommendResponseDto.FacilityDto.builder()
                                .restaurant(categories.getRestaurant())
                                .hospital(categories.getHospital())
                                .cafe(categories.getCafe())
                                .pharmacy(categories.getPharmacy())
                                .mart(categories.getMart()).build()
                ).build();
    }

}
