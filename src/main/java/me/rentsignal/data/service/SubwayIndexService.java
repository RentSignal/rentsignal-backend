package me.rentsignal.data.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rentsignal.data.dto.IndexApiResponseDto;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
import me.rentsignal.location.entity.District;
import me.rentsignal.location.repository.DistrictRepository;
import me.rentsignal.locationInfo.entity.DistrictIndex;
import me.rentsignal.locationInfo.repository.DistrictIndexRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubwayIndexService {

    @Value("${SUBWAY_ACCESSIBILITY_INDEX_API_URL}")
    private String SUBWAY_ACCESSIBILITY_INDEX_API_URL;

    public static final List<String> CITIES = List.of("성남", "수원", "안양", "고양", "용인", "부천", "안산", "천안", "화성", "창원", "포항", "전주", "청주");

    private final ObjectMapper objectMapper;
    private final DistrictIndexRepository districtIndexRepository;
    private final DistrictRepository districtRepository;

    @Transactional
    public void saveSubwayAccessibilityIndex() {
        // 1. 외부 API에서 지하철 역세권 지수 데이터 조회
        List<IndexApiResponseDto.Row> rows = getSubwayIndexRows();

        // 2. DB에서 District를 { 'Province 이름 > District 이름', District } 형식의 key로 조회할 수 있도록 Map 생성
        Map<String, District> provinceAndDistrictMap = districtRepository.findAll().stream()
                .collect(Collectors.toMap(
                        d -> d.getProvince().getName() + ">" + d.getName(), d -> d
                ));


        for (IndexApiResponseDto.Row row : rows) {
            // 3-1. 외부 API 데이터의 지역명을 DB 형식에 맞게 변환 (key 생성)
            String fullName = row.getFullName();
            if (fullName == null || fullName.isBlank()) {
                throw new BaseException(ErrorCode.EXTERNAL_API_ERROR, "데이터의 CLS_FULLNM이 비어있습니다.");
            }

            String[] parts = fullName.split(">");
            if (parts.length != 2) {
                throw new BaseException(ErrorCode.EXTERNAL_API_ERROR, "잘못된 CLS_FULLNM 형식입니다. - " + fullName);
            }

            String provinceName = convertProvinceName(parts[0].trim());
            String districtName = convertDistrictName(parts[1].trim());

            String key = provinceName + ">" + districtName;

            // 3-2. key에 해당하는 province + district 조합 있는지 확인
            District district = provinceAndDistrictMap.get(key);
            if (district == null) {
                throw new BaseException(ErrorCode.DISTRICT_NOT_FOUND, "해당 시/군/구를 찾을 수 없습니다. - " + key);
            }

            // DTA_VAL 값이 null인 데이터는 저장 X
            BigDecimal value = row.getValue();
            if (value == null) continue;

            try {
                districtIndexRepository.save(DistrictIndex.builder()
                        .district(district)
                        .subwayAccessibilityIndex(value.setScale(1, RoundingMode.HALF_UP))
                        .baseYearMonth(row.getDate()).build());
            } catch (DataIntegrityViolationException e) {
                throw new BaseException(ErrorCode.DUPLICATED_DATA, "해당 시/군/구에 해당 기간의 지하철 역세권 지수가 이미 존재합니다. - " + district.getName());
            }
        }
    }

    // 외부 API 호출해 데이터 조회 후 Row에 매핑
    private List<IndexApiResponseDto.Row> getSubwayIndexRows() {
        RestTemplate restTemplate = new RestTemplate();

        IndexApiResponseDto indexApiResponseDto;

        try {
            String responseBody = restTemplate
                    .exchange(SUBWAY_ACCESSIBILITY_INDEX_API_URL, HttpMethod.GET, null, String.class).getBody();

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

        return indexApiResponseDto.getSttsApiTblData().stream()
                .filter(data -> data.getRow() != null)
                .findFirst()
                .orElseThrow(() -> new BaseException(ErrorCode.EXTERNAL_API_ERROR, "지수 데이터 row가 없습니다."))
                .getRow();
    }

    // 시/도 이름 약어 -> 정식 시/도 이름으로 변환
    private String convertProvinceName(String rawName) {
        return switch (rawName) {
            case "서울" -> "서울특별시";
            case "대전" -> "대전광역시";
            case "울산" -> "울산광역시";
            case "광주" -> "광주광역시";
            case "인천" -> "인천광역시";
            case "대구" -> "대구광역시";
            case "부산" -> "부산광역시";
            case "경북" -> "경상북도";
            case "경남" -> "경상남도";
            case "강원" -> "강원특별자치도";
            case "경기" -> "경기도";
            case "충남" -> "충청남도";
            default -> rawName;
        };
    }

    // 시/군/구 이름 중 XXOO구를 XX시OO구로 변환 (데이터 형식 통일)
    private String convertDistrictName(String rawName) {
        for (String city : CITIES) {
            if (rawName.startsWith(city) && rawName.endsWith("구"))
                return city + "시" + rawName.substring(city.length());
        }

        return rawName;
    }

}
