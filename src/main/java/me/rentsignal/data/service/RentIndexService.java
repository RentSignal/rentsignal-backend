package me.rentsignal.data.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rentsignal.data.dto.IndexApiResponseDto;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
import me.rentsignal.location.entity.Region;
import me.rentsignal.location.repository.RegionRepository;
import me.rentsignal.locationInfo.entity.HousingType;
import me.rentsignal.locationInfo.entity.RegionIndex;
import me.rentsignal.locationInfo.repository.RegionIndexRepository;
import me.rentsignal.recommendation.dto.AiRecommendRequestDto;
import me.rentsignal.user.entity.Role;
import me.rentsignal.user.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.RoundingMode;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
/**
 * 지수 데이터 (소비자 심리지수, 전월세 통합지수, 지하철 역세권 지수) 저장
 */
public class RentIndexService {

    private final ObjectMapper objectMapper;
    @Value("${VILLA_RENT_INDEX_API_URL}")
    private String VILLA_RENT_INDEX_API_URL;

    @Value("${APT_RENT_INDEX_API_URL}")
    private String APT_RENT_INDEX_API_URL;

    private final RegionIndexRepository regionIndexRepository;
    private final RegionRepository regionRepository;
    private final AuthService authService;

    public void saveRentCompositeIndex(Long userId, HousingType housingType) {
        authService.validateUserAccess(userId, Role.ROLE_ADMIN);

        // 서울 > 강북지역 > 도심권
        Region northCentral = findRegionByAreaGroupAndAreaName("강북", "도심권");
        saveRegionIndex(northCentral, housingType,
                getFilteredRentIndex("520010", housingType));


        // 서울 > 강북지역 > 동북권
        Region northNorthEast = findRegionByAreaGroupAndAreaName("강북", "동북권");
        saveRegionIndex(northNorthEast, housingType,
                getFilteredRentIndex("520011", housingType));

        // 서울 > 강북지역 > 서북권
        Region northNorthWest = findRegionByAreaGroupAndAreaName("강북", "서북권");
        saveRegionIndex(northNorthWest, housingType,
                getFilteredRentIndex("520012", housingType));

        // 서울 > 강남지역 > 서남권
        Region southSouthWest = findRegionByAreaGroupAndAreaName("강남", "서남권");
        saveRegionIndex(southSouthWest, housingType,
                getFilteredRentIndex("520014", housingType));

        // 서울 > 강남지역 > 동남권
        Region southSouthEast = findRegionByAreaGroupAndAreaName("강남", "동남권");
        saveRegionIndex(southSouthEast, housingType,
                getFilteredRentIndex("520015", housingType));

    }

    // 권역 조회
    private Region findRegionByAreaGroupAndAreaName(String areaGroup, String areaName) {
        return regionRepository.findByAreaGroupAndAreaName(areaGroup, areaName)
                .orElseThrow(() -> new BaseException(ErrorCode.REGION_NOT_FOUND, "해당 권역을 찾을 수 없습니다. - " + areaGroup + " " + areaName));
    }

    // 1년 전까지의 전월세 통합지수 조회
    private List<IndexApiResponseDto.Row> getFilteredRentIndex(String classificationId, HousingType housingType) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<AiRecommendRequestDto> requestEntity = new HttpEntity<>(headers);

        IndexApiResponseDto indexApiResponseDto;

        String API_URL;
        if (housingType == HousingType.APARTMENT) {
            API_URL = APT_RENT_INDEX_API_URL + classificationId;
        } else if (housingType == HousingType.MULTI_FAMILY_HOUSE) {
            API_URL = VILLA_RENT_INDEX_API_URL + classificationId;
        } else {
            throw new BaseException(ErrorCode.INVALID_HOUSING_TYPE, "잘못된 housing type입니다. - " + housingType.name());
        }

        try {

            String responseBody = restTemplate
                    .exchange(API_URL, HttpMethod.GET, requestEntity, String.class).getBody();

            if (responseBody == null || responseBody.isBlank())
                throw new BaseException(ErrorCode.EXTERNAL_API_ERROR, "외부 API로부터 응답을 받아오지 못했습니다.");

            indexApiResponseDto = objectMapper.readValue(responseBody, IndexApiResponseDto.class);

        } catch (ResourceAccessException e) {
            log.error("외부 API 연결 에러 - " + e.getMessage());
            throw new BaseException(ErrorCode.EXTERNAL_API_ERROR, "외부 API 연결에 실패했습니다.");
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("외부 API 에러 - " + e.getMessage());
            throw new BaseException(ErrorCode.EXTERNAL_API_ERROR, "외부 API에서 알 수 없는 오류가 발생했습니다.");
        }

        List<IndexApiResponseDto.Row> rows = indexApiResponseDto.getSttsApiTblData().stream()
                .filter(data -> data.getRow() != null)
                .findFirst()
                .orElseThrow(() -> new BaseException(ErrorCode.EXTERNAL_API_ERROR, "지수 데이터 row가 없습니다."))
                .getRow();

        return rows.stream()
                .filter(row -> row.getDate() != null)
                .filter(row -> {
                    String date = row.getDate();
                    return date.compareTo("202502") >=0 && date.compareTo("202602") <=0;
                }).toList();
    }

    private void saveRegionIndex(Region region, HousingType housingType,
                                 List<IndexApiResponseDto.Row> rows) {
        for (IndexApiResponseDto.Row row : rows) {
            try {
                regionIndexRepository.save(RegionIndex.builder()
                        .region(region)
                        .housingType(housingType)
                        .rentCompositeIndex(row.getValue().setScale(1, RoundingMode.HALF_UP))
                        .baseYearMonth(row.getDate()).build());
            } catch (DataIntegrityViolationException e) {
                throw new BaseException(ErrorCode.DUPLICATED_DATA, "해당 권역에 해당 기간의 "+ housingType + " 전월세 통합지수가 이미 존재합니다. - " + region.getAreaGroup() + " " + region.getAreaName());
            }
        }
    }

}
