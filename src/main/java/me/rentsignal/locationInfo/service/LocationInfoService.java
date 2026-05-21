package me.rentsignal.locationInfo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class LocationInfoService {

    public BigDecimal calculateChangeRate(BigDecimal baseValue, BigDecimal comparisonValue) {
        return baseValue
                .subtract(comparisonValue)
                .divide(comparisonValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);
    }

}
