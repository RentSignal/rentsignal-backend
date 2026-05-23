package me.rentsignal.locationInfo.service;

import lombok.RequiredArgsConstructor;
import me.rentsignal.locationInfo.dto.DistrictSafetyDto;
import me.rentsignal.locationInfo.dto.RankItemDto;
import me.rentsignal.locationInfo.entity.DistrictSafety;
import me.rentsignal.locationInfo.repository.DistrictSafetyRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SafetyService {

    private final DistrictSafetyRepository districtSafetyRepository;

    public DistrictSafetyDto getSafety() {
        List<DistrictSafety> districtSafetyList = districtSafetyRepository.findByDistrict_Province_NameOrderBySafetyScoreDesc("서울특별시");

        List<DistrictSafetyDto.DistrictSafetyScoreDto> districtSafetyScores = new ArrayList<>();
        List<RankItemDto> ranking = new ArrayList<>();
        int i = 1;

        for (DistrictSafety districtSafety : districtSafetyList) {
            String districtName = districtSafety.getDistrict().getName();
            BigDecimal safetyScore = BigDecimal.valueOf(districtSafety.getSafetyScore()).multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP);

            districtSafetyScores.add(new DistrictSafetyDto.DistrictSafetyScoreDto(
                    districtName,
                    safetyScore
            ));

            if (i < 8) {
                ranking.add(new RankItemDto(
                        i,
                        districtName,
                        safetyScore
                ));
            }

            i++;
        }

        return new DistrictSafetyDto(
                ranking,
                districtSafetyScores
        );
    }

}
