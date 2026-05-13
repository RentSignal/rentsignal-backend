package me.rentsignal.data.service;

import lombok.RequiredArgsConstructor;
import me.rentsignal.data.dto.DistrictSafetyDto;
import me.rentsignal.data.dto.SafetyRowDto;
import me.rentsignal.data.reader.SafetyCsvReader;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
import me.rentsignal.location.entity.District;
import me.rentsignal.location.repository.DistrictRepository;
import me.rentsignal.locationInfo.entity.DistrictSafety;
import me.rentsignal.locationInfo.repository.DistrictSafetyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SafetyDataService {

    private final SafetyCsvReader safetyCsvReader;
    private final DistrictRepository districtRepository;
    private final DistrictSafetyRepository districtSafetyRepository;

    @Transactional
    public void saveSafety() {
        Map<String, District> districtMapByName = loadDistrictMapByName();
        // 중복 방지를 위해 현재 저장된 DistrictSafety 모두 조회
        Map<String, DistrictSafety> districtSafetyMapByName = loadDistrictSafetyMapByName();

        // 1. CCTV 데이터 조회
        List<SafetyRowDto> cctvRowDtos = safetyCsvReader.read("cctv.csv");

        // 2. 범죄건수 조회
        List<SafetyRowDto> crimeRowDtos = safetyCsvReader.read("crime.csv");

        // 3. districtName 기준으로 합치기
        Map<String, DistrictSafetyDto> safetyDtoMap = new HashMap<>();

        // 3-1. cctv 데이터 먼저 넣기
        for (SafetyRowDto row : cctvRowDtos) {
            safetyDtoMap.put(row.getDistrictName(),
                    DistrictSafetyDto.builder()
                            .districtName(row.getDistrictName())
                            .cctvCount(row.getCount()).build());
        }

        // 3-2. crime 데이터와 합치기
        for (SafetyRowDto row : crimeRowDtos) {
            safetyDtoMap.compute(row.getDistrictName(), (key, value) -> {
                if (value == null) {
                    return DistrictSafetyDto.builder()
                            .districtName(row.getDistrictName())
                            .cctvCount(0)
                            .crimeCount(row.getCount()).build();
                }
                value.setCrimeCount(row.getCount());
                return value;
            });
        }

        // 4. cctv 수 정규화를 위한 최대, 최소 계산
        List<DistrictSafetyDto> safetyDtos = new ArrayList<>(safetyDtoMap.values());

        int minCctvCount = safetyDtos.stream()
                .mapToInt(DistrictSafetyDto::getCctvCount)
                .min().orElse(0);

        int maxCctvCount = safetyDtos.stream()
                .mapToInt(DistrictSafetyDto::getCctvCount)
                .max().orElse(0);

        // 5. 범죄건수 역수 정규화를 위한 최대, 최소
        double minCrimeInverse = safetyDtos.stream()
                .mapToDouble(dto -> toCrimeInverse(dto.getCrimeCount()))
                .min().orElse(0);

        double maxCrimeInverse = safetyDtos.stream()
                .mapToDouble(dto -> toCrimeInverse(dto.getCrimeCount()))
                .max().orElse(0);

        // 6. 치안 점수 계산 & 저장
        for (DistrictSafetyDto dto : safetyDtos) {
            // 치안 점수 = CCTV 정규화 * 0.5 + 범죄건수 역수 정규화 * 0.5
            double cctvScore = normalize(
                    dto.getCctvCount(),
                    minCctvCount,
                    maxCctvCount
            );

            double crimeInverse = toCrimeInverse(dto.getCrimeCount());
            double crimeScore = normalize(
                    crimeInverse,
                    minCrimeInverse,
                    maxCrimeInverse
            );

            double safetyScore = cctvScore * 0.5 + crimeScore * 0.5;

            // 6-1. DB에서 해당 이름의 District 조회
            String districtName = dto.getDistrictName();
            District district = districtMapByName.get(districtName);
            if (district == null) {
                throw new BaseException(ErrorCode.DISTRICT_NOT_FOUND, "해당 이름의 시/군/구를 찾을 수 없습니다. - " + districtName);
            }

            // 6-2. 해당 districtName의 DistrictSafety 조회
            DistrictSafety districtSafety = districtSafetyMapByName.get(districtName);
            if (districtSafety == null) {
                districtSafetyRepository.save(DistrictSafety.builder()
                        .district(district)
                        .cctvCount(dto.getCctvCount())
                        .crimeCount(dto.getCrimeCount())
                        .safetyScore(safetyScore).build());
            } else { // 이미 존재하는 경우 update
                districtSafety.update(
                        dto.getCctvCount(),
                        dto.getCrimeCount(),
                        safetyScore
                );
            }
        }
    }

    /** 범죄건수 역수 계산 */
    private double toCrimeInverse(int crimeCount) {
        return crimeCount <= 0
                ? 0.0
                : 1.0 / crimeCount;
    }

    private double normalize(double value, double min, double max) {
        return max == min
                ? 0.0
                : (value - min) / (max - min);
    }

    private Map<String, District> loadDistrictMapByName() {
        return districtRepository.findAll().stream()
                .filter(d -> d.getProvince().getName().equals("서울특별시"))
                .collect(Collectors.toMap(District::getName, d -> d));
    }

    private Map<String, DistrictSafety> loadDistrictSafetyMapByName() {
        return districtSafetyRepository.findAll().stream()
                .filter(ds -> ds.getDistrict().getProvince().getName().equals("서울특별시"))
                .collect(Collectors.toMap(
                        ds -> ds.getDistrict().getName(),
                        ds -> ds));
    }

}
