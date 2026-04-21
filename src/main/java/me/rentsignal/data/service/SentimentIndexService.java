package me.rentsignal.data.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rentsignal.data.dto.SentimentIndexApiResponseDto;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
import me.rentsignal.location.entity.Province;
import me.rentsignal.location.repository.ProvinceRepository;
import me.rentsignal.locationInfo.entity.ProvinceIndex;
import me.rentsignal.locationInfo.repository.ProvinceIndexRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SentimentIndexService {

    private final ProvinceRepository provinceRepository;
    private final ProvinceIndexRepository provinceIndexRepository;
    private final ObjectMapper objectMapper;

    @Value("${CONSUMER_SENTIMENT_INDEX_API_URL}")
    private String CONSUMER_SENTIMENT_INDEX_API_URL;

    @Transactional
    public void saveConsumerSentimentIndex() {
        RestTemplate restTemplate = new RestTemplate();

        List<SentimentIndexApiResponseDto> indexList;

        try {
            String responseBody = restTemplate
                    .exchange(CONSUMER_SENTIMENT_INDEX_API_URL, HttpMethod.GET, null, String.class).getBody();

            if (responseBody == null || responseBody.isBlank())
                throw new BaseException(ErrorCode.EXTERNAL_API_ERROR, "외부 API로부터 응답을 받아오지 못했습니다.");

            indexList = objectMapper.readValue(responseBody, new com.fasterxml.jackson.core.type.TypeReference<List<SentimentIndexApiResponseDto>>() {});
        } catch (ResourceAccessException e) {
            log.error("외부 API 연결 에러 - " + e.getMessage());
            throw new BaseException(ErrorCode.EXTERNAL_API_ERROR, "외부 API 연결에 실패했습니다.");
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("외부 API 에러 - " + e.getMessage());
            throw new BaseException(ErrorCode.EXTERNAL_API_ERROR, "외부 API에서 알 수 없는 오류가 발생했습니다.");
        }

        // Province 미리 불러오기
        Map<String, Province> provinceMap = provinceRepository.findAll().stream()
                .collect(Collectors.toMap(Province::getName, p -> p));

        if (indexList == null)
            throw new BaseException(ErrorCode.EXTERNAL_API_ERROR, "소비자 심리지수 데이터가 없습니다.");

        for (SentimentIndexApiResponseDto index : indexList) {
            Province province = provinceMap.get(index.getProvinceName());

            // 수도권, 전국 등 시/도가 아닌 값은 패스
            if (province == null) continue;

            try {
                provinceIndexRepository.save(
                        ProvinceIndex.builder()
                                .province(province)
                                .consumerSentimentIndex(index.getValue())
                                .baseYearMonth(index.getDate()).build()
                );
            } catch (DataIntegrityViolationException e) {
                throw new BaseException(ErrorCode.DUPLICATED_DATA, "해당 시/도에 해당 기간의 소비자 심리지수가 이미 존재합니다. - " + province.getName());
            }
        }
    }
}
