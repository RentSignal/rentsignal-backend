package me.rentsignal.locationInfo.util;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public final class YearMonthUtils {

    private YearMonthUtils() {}

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    public static YearMonth toYearMonth(String value) {
        return YearMonth.parse(value, FORMATTER);
    }

    public static String formatYearMonth(YearMonth value) {
        return value.format(FORMATTER);
    }

}
