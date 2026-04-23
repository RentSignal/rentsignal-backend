package me.rentsignal.data.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rentsignal.data.dto.NeighborhoodBoundaryApiResponseDto;
import me.rentsignal.data.external.ExternalApiClient;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
import me.rentsignal.location.entity.Neighborhood;
import me.rentsignal.location.entity.NeighborhoodBoundary;
import me.rentsignal.location.repository.NeighborhoodBoundaryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NeighborhoodBoundaryDataService {

    private final LegalDongImportService legalDongImportService;
    @Value("${VWORLD_API_BASE_URL}")
    private String VWORLD_API_BASE_URL;

    private final NeighborhoodBoundaryRepository neighborhoodBoundaryRepository;
    private final ObjectMapper objectMapper;
    private final ExternalApiClient externalApiClient;

    // 대한민국을 덮는 최소 bounding box를 3x3으로 분할
    private static final List<String> GEOM_FILTERS = List.of(
            "BOX(124, 33, 126.67, 36.5)",
            "BOX(126.67, 33, 129.33, 36.5)",
            "BOX(129.33, 33, 132, 36.5)",
            "BOX(124, 36.5, 126.67, 40)",
            "BOX(126.67, 36.5, 129.33, 40)",
            "BOX(129.33, 36.5, 132, 40)",
            "BOX(124, 40, 126.67, 43)",
            "BOX(126.67, 40, 129.33, 43)",
            "BOX(129.33, 40, 132, 43)"
    );

    /** 좌표 -> 읍면동 매핑 위해 vworld API에서 읍면동 경계 데이터 조회 후 저장 */
    @Transactional
    public void saveNeighborhoodBoundaries() {
        Set<String> savedCodes = new HashSet<>(neighborhoodBoundaryRepository.findAllNeighborhoodCodes());
        Map<String, Neighborhood> neighborhoodMap = legalDongImportService.loadNeighborhoodMap();

        for (String geomFilter : GEOM_FILTERS) {
            saveBoundariesByGeomFilter(geomFilter, savedCodes, neighborhoodMap);
        }
    }

    private void saveBoundariesByGeomFilter(String geomFilter, Set<String> savedCodes, Map<String, Neighborhood> neighborhoodMap) {
        int page = 1;
        int totalPage = Integer.MAX_VALUE;

        while (page <= totalPage) {
            String API_URL = UriComponentsBuilder.fromHttpUrl(VWORLD_API_BASE_URL)
                    .queryParam("size", 1000)
                    .queryParam("page", page)
                    .queryParam("geomFilter", geomFilter)
                    .build().toUriString();

            NeighborhoodBoundaryApiResponseDto responseDto
                    = externalApiClient.getResponse(API_URL, NeighborhoodBoundaryApiResponseDto.class);

            NeighborhoodBoundaryApiResponseDto.ResponseDto response = responseDto.getResponse();
            if (response == null || response.getPage() == null) {
                throw new BaseException(ErrorCode.EXTERNAL_API_ERROR, "읍면동 경계 API 응답 형식이 올바르지 않습니다.");
            }

            // totalPage 갱신
            if (page == 1) {
                totalPage = response.getPage().getTotal();
            }

            NeighborhoodBoundaryApiResponseDto.ResultDto result = response.getResult();
            if (result == null || result.getFeatureCollection() == null
                    || result.getFeatureCollection().getFeatures() == null || result.getFeatureCollection().getFeatures().isEmpty()) {
                page ++;
                continue;
            }

            // Feature -> NeighborhoodBoundary로 매핑
            List<NeighborhoodBoundary> boundaries = result.getFeatureCollection().getFeatures().stream()
                    .map(f -> toNeighborhoodBoundary(f, neighborhoodMap))
                    .filter(Objects::nonNull)
                    .filter(boundary -> savedCodes.add(boundary.getNeighborhood().getCode()))
                    .toList();

            if (!boundaries.isEmpty()) {
                neighborhoodBoundaryRepository.saveAll(boundaries);
                log.info("geomFilter = {}, page = {}, saved = {}개", geomFilter, page, boundaries.size());
            } else {
                log.info("geomFilter = {}, page = {}, saved = 0개", geomFilter, page);
            }

            page++;
        }

    }

    private NeighborhoodBoundary toNeighborhoodBoundary(NeighborhoodBoundaryApiResponseDto.FeatureDto featureDto, Map<String, Neighborhood> neighborhoodMap) {
        NeighborhoodBoundaryApiResponseDto.PropertiesDto propertiesDto = featureDto.getProperties();
        NeighborhoodBoundaryApiResponseDto.GeometryDto geometryDto = featureDto.getGeometry();

        if (propertiesDto == null || geometryDto == null) {
            throw new BaseException(ErrorCode.EXTERNAL_API_ERROR, "읍면동 경계 데이터의 properties나 geometry가 누락되었습니다.");
        }

        String emdCode = propertiesDto.getEmdCode();
        String fullName = propertiesDto.getFullName();
        if (emdCode == null || emdCode.isBlank()) {
            throw new BaseException(ErrorCode.EXTERNAL_API_ERROR, "읍면동의 emdCode가 비어있습니다. - " + fullName);
        }

        // vworld 읍면동 코드는 8자리, DB에는 10자리 -> DB 포맷에 맞게 변환
        String legalDongCode = emdCode + "00";

        Neighborhood neighborhood = neighborhoodMap.get(legalDongCode);
        if (neighborhood == null) {
            log.warn("해당 코드의 읍면동이 존재하지 않습니다. - {}, {}", emdCode, fullName);
            return null;
        }

        return NeighborhoodBoundary.builder()
                .neighborhood(neighborhood)
                .geometryJson(convertGeometryToJson(geometryDto)).build();
    }

    /** 저장을 위해 GeometryDTO를 String으로 변환 */
    private String convertGeometryToJson(NeighborhoodBoundaryApiResponseDto.GeometryDto geometryDto) {
        try {
            return objectMapper.writeValueAsString(geometryDto);
        } catch (Exception e) {
            throw new BaseException(ErrorCode.EXTERNAL_API_ERROR, "읍면동 경계 geometry -> 문자열 변환에 실패했습니다.");
        }
    }

}
