package me.rentsignal.locationInfo.service;

import lombok.RequiredArgsConstructor;
import me.rentsignal.locationInfo.dto.IndexItemDto;
import me.rentsignal.locationInfo.dto.SubwayAccessibilityIndexDto;
import me.rentsignal.locationInfo.entity.DistrictIndex;
import me.rentsignal.locationInfo.repository.DistrictIndexRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubwayAccessibilityIndexService {

    private final DistrictIndexRepository districtIndexRepository;
    private final LocationInfoService locationInfoService;

    public SubwayAccessibilityIndexDto getSubwayAccessibilityIndex() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");

        // 데이터가 2개월 지연되어 제공되기 때문에 2개월 전 데이터 사용
        YearMonth baseYearMonth = YearMonth.now().minusMonths(2);
        YearMonth comparisonYearMonth = baseYearMonth.minusMonths(6);

        String base = baseYearMonth.format(formatter);
        String comparison = comparisonYearMonth.format(formatter);

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
        List<IndexItemDto> high = new ArrayList<>();
        List<IndexItemDto> changeRate = new ArrayList<>();
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
            high.add(new IndexItemDto(
                    i,
                    districtName,
                    districtIndex.getSubwayAccessibilityIndex().setScale(1, RoundingMode.HALF_UP)
            ));
            i++;
            // 6개월 전 기준 지수 증감률 내림차순
            DistrictIndex previousIndex = previousIndexMap.get(districtId);
            if (previousIndex == null) {
                continue;
            }

            BigDecimal rate = locationInfoService.calculateChangeRate(districtIndex.getSubwayAccessibilityIndex(),
                    previousIndex.getSubwayAccessibilityIndex());
            changeRate.add(new IndexItemDto(0,
                    districtName,
                    rate.setScale(1, RoundingMode.HALF_UP)));
        }

        changeRate.sort((a, b) -> b.value().compareTo(a.value()));

        List<IndexItemDto> rankedchangeRate = new ArrayList<>();
        for (int j = 0; j < changeRate.size(); j++) {
            IndexItemDto item = changeRate.get(j);

            rankedchangeRate.add(new IndexItemDto(
                    j + 1,
                    item.name(),
                    item.value()
            ));
        }

        return new SubwayAccessibilityIndexDto(
                high,
                rankedchangeRate,
                indexes
        );
    }

}
