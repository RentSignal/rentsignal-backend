package me.rentsignal.locationInfo.service;

import lombok.RequiredArgsConstructor;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
import me.rentsignal.location.entity.District;
import me.rentsignal.location.repository.DistrictRepository;
import me.rentsignal.locationInfo.dto.DistrictSubwayDto;
import me.rentsignal.locationInfo.dto.RankItemDto;
import me.rentsignal.locationInfo.dto.SubwayAccessibilityIndexDto;
import me.rentsignal.locationInfo.dto.SubwayStationDto;
import me.rentsignal.locationInfo.entity.DistrictIndex;
import me.rentsignal.locationInfo.entity.NeighborhoodTransport;
import me.rentsignal.locationInfo.entity.TransportType;
import me.rentsignal.locationInfo.repository.DistrictIndexRepository;
import me.rentsignal.locationInfo.repository.NeighborhoodTransportRepository;
import me.rentsignal.locationInfo.util.YearMonthUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubwayAccessibilityIndexService {

    private final DistrictIndexRepository districtIndexRepository;
    private final LocationInfoService locationInfoService;
    private final DistrictRepository districtRepository;
    private final NeighborhoodTransportRepository neighborhoodTransportRepository;

    @Cacheable(value = "subway-accessibility-index")
    public SubwayAccessibilityIndexDto getSubwayAccessibilityIndex() {
        // 데이터가 2개월 지연되어 제공되기 때문에 2개월 전 데이터 사용
        String base = districtIndexRepository.findLatestBaseYearMonth();
        YearMonth baseYearMonth = YearMonthUtils.toYearMonth(base);

        YearMonth comparisonYearMonth = baseYearMonth.minusMonths(6);
        String comparison = YearMonthUtils.formatYearMonth(comparisonYearMonth);

        // 서울특별시 데이터의 당월, 6개월 전 데이터 조회
        List<DistrictIndex> currentIndexes = districtIndexRepository.findByDistrict_Province_NameAndBaseYearMonthOrderBySubwayAccessibilityIndexDesc("서울특별시", base);
        List<DistrictIndex> previousIndexes = districtIndexRepository.findByDistrict_Province_NameAndBaseYearMonthOrderBySubwayAccessibilityIndexDesc("서울특별시", comparison);

        // districtId 기준으로 6개월 전 인덱스를 빠르게 조회하기 위한 맵
        Map<Long, DistrictIndex> previousIndexMap = previousIndexes.stream()
                .collect(Collectors.toMap(
                        index -> index.getDistrict().getId(),
                        index -> index
                ));

        List<SubwayAccessibilityIndexDto.DistrictIndexDto> indexes = new ArrayList<>();
        List<RankItemDto> high = new ArrayList<>();
        List<RankItemDto> changeRate = new ArrayList<>();
        int i = 1;
        for (DistrictIndex districtIndex : currentIndexes) {
            Long districtId = districtIndex.getDistrict().getId();
            String districtName = districtIndex.getDistrict().getName();
            // 각 District별 지수 데이터 DTO
            indexes.add(new SubwayAccessibilityIndexDto.DistrictIndexDto(
                    districtId,
                    districtName,
                    districtIndex.getSubwayAccessibilityIndex().setScale(1, RoundingMode.HALF_UP)
            ));
            // 당월 지하철 역세권 지수 내림차순
            if (i < 8) {
                high.add(new RankItemDto(
                        i,
                        districtName,
                        districtIndex.getSubwayAccessibilityIndex().setScale(1, RoundingMode.HALF_UP)
                ));
            }
            i++;
            // 6개월 전 기준 지수 증감률 내림차순
            DistrictIndex previousIndex = previousIndexMap.get(districtId);
            if (previousIndex == null) {
                continue;
            }

            BigDecimal rate = locationInfoService.calculateChangeRate(districtIndex.getSubwayAccessibilityIndex(),
                    previousIndex.getSubwayAccessibilityIndex());
            changeRate.add(new RankItemDto(0,
                    districtName,
                    rate));
        }

        changeRate.sort((a, b) -> b.value().compareTo(a.value()));

        List<RankItemDto> rankedChangeRate = new ArrayList<>();
        for (int j = 0; j < Math.min(7, changeRate.size()); j++) {
            RankItemDto item = changeRate.get(j);

            rankedChangeRate.add(new RankItemDto(
                    j + 1,
                    item.name(),
                    item.value()
            ));
        }

        return new SubwayAccessibilityIndexDto(
                high,
                rankedChangeRate,
                indexes
        );
    }

    @Cacheable(value = "district-subway-station", key = "#districtId")
    public DistrictSubwayDto getSubwayStationByDistrict(Long districtId) {
        District district = districtRepository.findById(districtId).orElseThrow(
                () -> new BaseException(ErrorCode.DISTRICT_NOT_FOUND, "해당 id의 시/군/구가 존재하지 않습니다."));

        List<NeighborhoodTransport> neighborhoodTransports = neighborhoodTransportRepository.findByNeighborhood_District_IdAndTransportType(districtId, TransportType.SUBWAY_STATION);
        List<SubwayStationDto> subwayStations = new ArrayList<>();
        for (NeighborhoodTransport transport : neighborhoodTransports) {
            String name = transport.getName();
            String[] arr = name.split("\\|");
            String stationName = arr[0];
            String lineName = arr[1];
            subwayStations.add(new SubwayStationDto(
                    lineName,
                    stationName
            ));
        }

        String baseYearMonth = districtIndexRepository.findLatestBaseYearMonth();
        DistrictIndex index = districtIndexRepository.findByDistrict_IdAndBaseYearMonth(districtId, baseYearMonth).orElse(null);
        BigDecimal value = (index != null) ? index.getSubwayAccessibilityIndex().setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return new DistrictSubwayDto(
                district.getName(),
                value,
                subwayStations
        );
    }

}
