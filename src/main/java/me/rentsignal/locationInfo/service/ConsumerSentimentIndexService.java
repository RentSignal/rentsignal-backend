package me.rentsignal.locationInfo.service;

import lombok.RequiredArgsConstructor;
import me.rentsignal.locationInfo.dto.ConsumerSentimentIndexDto;
import me.rentsignal.locationInfo.entity.ProvinceIndex;
import me.rentsignal.locationInfo.repository.ProvinceIndexRepository;
import me.rentsignal.locationInfo.type.PeriodType;
import me.rentsignal.locationInfo.util.YearMonthUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsumerSentimentIndexService {

    private final ProvinceIndexRepository provinceIndexRepository;
    private final LocationInfoService locationInfoService;

    // 현재는 서울특별시 데이터만 반환
    public ConsumerSentimentIndexDto getConsumerSentimentIndex(PeriodType periodType) {
        // 데이터가 2개월 지연되어 제공되기 때문에 2개월 전 데이터 사용
        String baseYearMonthText = provinceIndexRepository.findLatestBaseYearMonth();
        YearMonth baseYearMonth = YearMonthUtils.toYearMonth(baseYearMonthText);
        YearMonth yearMonth = periodType.toComparisonYearMonth(baseYearMonth);

        ProvinceIndex baseIndex = provinceIndexRepository.findByProvince_NameAndBaseYearMonth("서울특별시", baseYearMonthText).orElse(null);
        BigDecimal baseValue = (baseIndex != null) ? baseIndex.getConsumerSentimentIndex() : BigDecimal.ZERO;

        BigDecimal value;
        String formattedYearMonth = YearMonthUtils.formatYearMonth(yearMonth);
        if (periodType == PeriodType.CURRENT) {
            value = baseValue.setScale(1, RoundingMode.HALF_UP);
        } else {
            ProvinceIndex comparisonIndex = provinceIndexRepository.findByProvince_NameAndBaseYearMonth("서울특별시", formattedYearMonth).orElse(null);
            BigDecimal comparisonValue = (comparisonIndex != null) ? comparisonIndex.getConsumerSentimentIndex() : BigDecimal.ZERO;

            value = locationInfoService.calculateChangeRate(baseValue, comparisonValue);
        }

        List<ConsumerSentimentIndexDto.MonthlyConsumerSentimentIndexDto> trend = getConsumerSentimentIndexTrend(baseYearMonth);

        return new ConsumerSentimentIndexDto(
                trend,
                formattedYearMonth.substring(0, 4),
                formattedYearMonth.substring(4),
                value
        );
    }

    /** baseYearMonth로부터 6개월 전까지의 데이터 조회 */
    private List<ConsumerSentimentIndexDto.MonthlyConsumerSentimentIndexDto> getConsumerSentimentIndexTrend(YearMonth baseYearMonth) {
        String end = YearMonthUtils.formatYearMonth(baseYearMonth);
        String start = YearMonthUtils.formatYearMonth(baseYearMonth.minusMonths(6));

        List<ProvinceIndex> indexes = provinceIndexRepository.findByProvince_NameAndBaseYearMonthBetweenOrderByBaseYearMonthAsc("서울특별시", start, end);

        List<ConsumerSentimentIndexDto.MonthlyConsumerSentimentIndexDto> list = new ArrayList<>();
        for (ProvinceIndex index : indexes) {
            list.add(
                    new ConsumerSentimentIndexDto.MonthlyConsumerSentimentIndexDto(
                            index.getBaseYearMonth().substring(0, 4) + ". " + index.getBaseYearMonth().substring(4),
                            index.getConsumerSentimentIndex().setScale(1, RoundingMode.HALF_UP)
                    )
            );
        }

        return list;
    }

}
