package me.rentsignal.locationInfo.service;

import lombok.RequiredArgsConstructor;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
import me.rentsignal.locationInfo.dto.CurrentRentIndexDto;
import me.rentsignal.locationInfo.dto.RentIndexChangeDto;
import me.rentsignal.locationInfo.dto.RankItemDto;
import me.rentsignal.locationInfo.entity.HousingType;
import me.rentsignal.locationInfo.entity.RegionIndex;
import me.rentsignal.locationInfo.repository.RegionIndexRepository;
import me.rentsignal.locationInfo.type.PeriodType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentIndexService {

    private final RegionIndexRepository regionIndexRepository;
    private final LocationInfoService locationInfoService;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");

    public CurrentRentIndexDto getCurrentRentIndex(HousingType housingType) {
        // 데이터가 1개월 지연되어 제공되기 때문에 1개월 전 데이터 사용
        YearMonth now = YearMonth.now().minusMonths(1);

        List<RegionIndex> indexes = regionIndexRepository.findByHousingTypeAndBaseYearMonthOrderByRentCompositeIndexDesc(housingType, now.format(formatter));

        List<RankItemDto> list = new ArrayList<>();
        int i = 1;
        for (RegionIndex index : indexes) {
            list.add(new RankItemDto(

                    i,
                    getRegionName(index),
                    index.getRentCompositeIndex().setScale(1, RoundingMode.HALF_UP)
            ));
            i++;
        }

        return new CurrentRentIndexDto(list);
    }

    public RentIndexChangeDto getRentIndexChange(HousingType housingType, PeriodType periodType) {
        YearMonth baseYearMonth = YearMonth.now().minusMonths(1);

        if (periodType == PeriodType.CURRENT) {
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "해당 periodType을 지원하지 않습니다.");
        }
        YearMonth comparisonYearMonth = periodType.toComparisonYearMonth(baseYearMonth);

        List<RegionIndex> baseIndexes = regionIndexRepository.findByHousingTypeAndBaseYearMonth(housingType, baseYearMonth.format(formatter));
        List<RegionIndex> comparisonIndexes = regionIndexRepository.findByHousingTypeAndBaseYearMonth(housingType, comparisonYearMonth.format(formatter));

        Map<Long, RegionIndex> comparisonIndexMap = comparisonIndexes.stream()
                .collect(Collectors.toMap(
                        index -> index.getRegion().getId(),
                        index -> index));

        List<RankItemDto> riseList = new ArrayList<>();
        List<RankItemDto> fallList = new ArrayList<>();
        for (RegionIndex baseIndex : baseIndexes) {
            // comparisonIndexes에 해당 id의 region의 regionIndex가 존재하는지 확인
            RegionIndex comparisonIndex = comparisonIndexMap.get(baseIndex.getRegion().getId());

            if (comparisonIndex == null) {
                continue;
            }

            BigDecimal baseValue = baseIndex.getRentCompositeIndex();
            BigDecimal comparisonValue = comparisonIndex.getRentCompositeIndex();

            // 증감률 계산
            BigDecimal changeRate = locationInfoService.calculateChangeRate(baseValue, comparisonValue);

            // 증감률이 양수이면 riseList에, 음수이면 fallList에, 0이면 제외
            if (changeRate.compareTo(BigDecimal.ZERO) > 0) {
                riseList.add(new RankItemDto(
                        0,   // 순위는 임시로 0
                        getRegionName(baseIndex),
                        changeRate));
            } else if (changeRate.compareTo(BigDecimal.ZERO) < 0) {
                fallList.add(new RankItemDto(
                        0,
                        getRegionName(baseIndex),
                        changeRate));
            }
        }

        List<RankItemDto> sortedRiseList = riseList.stream()
                .sorted(
                        Comparator.comparing(RankItemDto::value)
                                .reversed()).toList();
        List<RankItemDto> sortedFallList = fallList.stream()
                .sorted(
                        Comparator.comparing(
                                RankItemDto::value)
                ).toList();

        return new RentIndexChangeDto(
                getRankedList(sortedRiseList),
                getRankedList(sortedFallList)
        );
    }

    private String getRegionName(RegionIndex index) {
        return index.getRegion().getAreaGroup() + " " + index.getRegion().getAreaName();
    }

    /** 정렬된 RentIndexItemDto 리스트 각 요소에 rank 반영해서 새로운 리스트 반환 */
    private List<RankItemDto> getRankedList(List<RankItemDto> sortedList) {
        List<RankItemDto> rankedList = new ArrayList<>();

        for (int i = 0; i < sortedList.size(); i++) {
            RankItemDto item = sortedList.get(i);

            rankedList.add(new RankItemDto(
                    i+1,
                    item.name(),
                    item.value()
            ));
        }

        return rankedList;
    }

}
