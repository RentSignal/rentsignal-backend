package me.rentsignal.locationInfo.dto;

import java.math.BigDecimal;
import java.util.List;

public record ConsumerSentimentIndexDto(
        List<MonthlyConsumerSentimentIndexDto> trend,
        String year,
        String month,
        BigDecimal value
) {

    public record MonthlyConsumerSentimentIndexDto(
            String yearMonth,
            BigDecimal value
    ) {
    }

}
