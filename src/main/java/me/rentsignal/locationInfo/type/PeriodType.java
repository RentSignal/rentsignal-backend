package me.rentsignal.locationInfo.type;

import java.time.YearMonth;

public enum PeriodType {
    ONE_YEAR,
    SIX_MONTH,
    ONE_MONTH,
    CURRENT;

    /** periodType에 따라 comparisonYearMonth 계산 */
    public YearMonth toComparisonYearMonth(YearMonth baseYearMonth) {
        return switch (this) {
            case ONE_YEAR -> baseYearMonth.minusYears(1);
            case SIX_MONTH -> baseYearMonth.minusMonths(6);
            case ONE_MONTH -> baseYearMonth.minusMonths(1);
            case CURRENT -> baseYearMonth;
        };
    }
}
