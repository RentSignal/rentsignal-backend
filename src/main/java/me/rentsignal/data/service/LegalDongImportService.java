package me.rentsignal.data.service;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.rentsignal.data.LegalDongCsvReader;
import me.rentsignal.data.dto.LegalDongCsvRowDto;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
import me.rentsignal.location.entity.District;
import me.rentsignal.location.entity.Neighborhood;
import me.rentsignal.location.entity.Province;
import me.rentsignal.location.entity.Ri;
import me.rentsignal.location.repository.DistrictRepository;
import me.rentsignal.location.repository.NeighborhoodRepository;
import me.rentsignal.location.repository.ProvinceRepository;
import me.rentsignal.location.repository.RiRepository;
import me.rentsignal.user.entity.Role;
import me.rentsignal.user.service.AuthService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class LegalDongImportService {

    private final LegalDongCsvReader legalDongCsvReader;
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final NeighborhoodRepository neighborhoodRepository;
    private final RiRepository riRepository;
    private final AuthService authService;

    /** csv 파일 읽어서 법정동 데이터 (코드, 시/도, 시/군/구, 읍/면/동, 리) 저장 */
    public void importLegalDongCsv(Long userId) {
        authService.validateUserAccess(userId, Role.ROLE_ADMIN);

        // 1. 편의를 위해 csv 한 행 -> LegalDongCsvRowDto로 변환
        List<LegalDongCsvRowDto> rows = legalDongCsvReader.read();

        // 2. DB 조회 최소화를 위해 행정구역 레벨별로 map 생성
        Map<String, Province> provinceMap = loadProvinceMap();
        Map<String, District> districtMap = loadDistrictMap();
        NeighborhoodMaps neighborhoodMaps = loadNeighborhoodMaps();
        Map<String, Neighborhood> neighborhoodCodeMap = neighborhoodMaps.getNeighborhoodCodeMap();
        Map<String, Neighborhood> neighborhoodKeyMap = neighborhoodMaps.getNeighborhoodKeyMap();
        Map<String, Ri> riMap = loadRiMap();

        // 3. csv 한 줄씩 처리 -> 저장되어있지 않던 데이터는 저장
        for (LegalDongCsvRowDto row : rows) {
            // csv 파일에는 행정구역 레벨이 다른 데이터가 섞여있음
            // (시/도만 있는 행, 시군구까지 있는 행, 읍면동까지 있는 행, 리까지 전부 있는 행)
            // 각 행의 행정구역 레벨 구분해서 처리 필요

            if (isProvinceRow(row)) { // 시/도 레벨
                saveProvince(row, provinceMap);
            }
            else if (isDistrictRow(row)) { // 시/군/구 레벨
                saveDistrict(row, provinceMap, districtMap);
            }
            else if (isNeighborhoodRow(row)) { // 읍/면/동 레벨
                saveNeighborhood(row, provinceMap, districtMap, neighborhoodCodeMap, neighborhoodKeyMap);
            }
            else if (isRiRow(row)) { // 리 레벨
                saveRi(row, provinceMap, districtMap, neighborhoodKeyMap, riMap);
            }
        }
    }

    // ---------- 행정구역 레벨별로 저장 ----------

    private Province saveProvince(LegalDongCsvRowDto row, Map<String, Province> map) {
        // 이미 해당 code의 Province가 존재할 경우 재사용
        Province province = map.get(row.getCode());
        if (province != null) return province;

        Province newProvince = provinceRepository.save(Province.builder()
                .name(row.getProvinceName())
                .code(row.getCode()).build());

        map.put(newProvince.getCode(), newProvince);
        return newProvince;
    }

    private District saveDistrict(LegalDongCsvRowDto row, Map<String, Province> provinceMap, Map<String, District> districtMap) {
        District district = districtMap.get(row.getCode());
        if (district != null) return district;

        Province province = findProvinceByName(provinceMap, row.getProvinceName());

        District newDistrict = districtRepository.save(District.builder()
                .name(row.getDistrictName())
                .code(row.getCode())
                .province(province).build());

        districtMap.put(newDistrict.getCode(), newDistrict);
        return newDistrict;
    }

    private Neighborhood saveNeighborhood(LegalDongCsvRowDto row,
                                          Map<String, Province> provinceMap,
                                          Map<String, District> districtMap,
                                          Map<String, Neighborhood> neighborhoodCodeMap,
                                          Map<String, Neighborhood> neighborhoodKeyMap) {
        Neighborhood neighborhoodByCode = neighborhoodCodeMap.get(row.getCode());
        if (neighborhoodByCode != null) return neighborhoodByCode;

        Province province = findProvinceByName(provinceMap, row.getProvinceName());
        District district = findDistrictByNameAndProvince(districtMap, province, row.getDistrictName());

        // 중복 (code는 다르고 같은 district 내 name이 동일한 경우)  방지를 위해 key 기준으로 한 번 더 체크
        String key = neighborhoodKey(district, row.getNeighborhoodName());
        Neighborhood neighborhoodByKey = neighborhoodKeyMap.get(key);
        if (neighborhoodByKey != null) { // 중복일 경우
            neighborhoodCodeMap.put(row.getCode(), neighborhoodByKey);
            return neighborhoodByKey;
        }

        Neighborhood newNeighborhood = neighborhoodRepository.save(Neighborhood.builder()
                .name(row.getNeighborhoodName())
                .code(row.getCode())
                .district(district).build());

        neighborhoodCodeMap.put(newNeighborhood.getCode(), newNeighborhood);
        neighborhoodKeyMap.put(key, newNeighborhood);
        return newNeighborhood;
    }

    private Ri saveRi(LegalDongCsvRowDto row,
                      Map<String, Province> provinceMap,
                      Map<String, District> districtMap,
                      Map<String, Neighborhood> neighborhoodKeyMap,
                      Map<String, Ri> riMap) {
        Ri ri = riMap.get(row.getCode());
        if (ri != null) return ri;

        Province province = findProvinceByName(provinceMap, row.getProvinceName());
        District district = findDistrictByNameAndProvince(districtMap, province, row.getDistrictName());

        String key = neighborhoodKey(district, row.getNeighborhoodName());
        Neighborhood neighborhood = neighborhoodKeyMap.get(key);
        if (neighborhood == null) {
            throw new BaseException(ErrorCode.NEIGHBORHOOD_NOT_FOUND, "해당 읍/면/동을 찾을 수 없습니다. - " + district.getName() + " " + row.getNeighborhoodName());
        }

        Ri newRi = riRepository.save(Ri.builder()
                .name(row.getRiName())
                .code(row.getCode())
                .neighborhood(neighborhood).build());

        riMap.put(newRi.getCode(), newRi);
        return newRi;
    }


    // ---------- 행정구역 레벨별 map에서 이름으로 조회 ----------
    private Province findProvinceByName(Map<String, Province> provinceMap, String name) {
        return provinceMap.values().stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new BaseException(ErrorCode.PROVINCE_NOT_FOUND, "해당 시/도를 찾을 수 없습니다. - " + name));
    }

    private District findDistrictByNameAndProvince(Map<String, District> districtMap, Province province, String name) {
        return districtMap.values().stream()
                .filter(d -> d.getProvince().getId().equals(province.getId()))
                .filter(d -> d.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new BaseException(ErrorCode.DISTRICT_NOT_FOUND, "해당 시/군/구를 찾을 수 없습니다. - " + province.getName() + " " + name));
    }



    // ---------- 해당 열이 특정 행정구역 레벨인지 판별 ----------

    private boolean isProvinceRow(LegalDongCsvRowDto row) {
        return hasText(row.getProvinceName())
                && !hasText(row.getDistrictName())
                && !hasText(row.getNeighborhoodName())
                && !hasText(row.getRiName());
    }

    private boolean isDistrictRow(LegalDongCsvRowDto row) {
        return hasText(row.getProvinceName())
                && hasText(row.getDistrictName())
                && !hasText(row.getNeighborhoodName())
                && !hasText(row.getRiName());
    }

    private boolean isNeighborhoodRow(LegalDongCsvRowDto row) {
        return hasText(row.getProvinceName())
                && hasText(row.getDistrictName())
                && hasText(row.getNeighborhoodName())
                && !hasText(row.getRiName());
    }

    private boolean isRiRow(LegalDongCsvRowDto row) {
        return hasText(row.getProvinceName())
                && hasText(row.getDistrictName())
                && hasText(row.getNeighborhoodName())
                && hasText(row.getRiName());
    }


    // ---------- 행정구역 레벨별로 저장된 데이터 전체 조회 후 Map으로 변환 ----------

    private Map<String, Province> loadProvinceMap() {
        return provinceRepository.findAll().stream()
                .collect(Collectors.toMap(Province::getCode, p -> p));
    }

    private Map<String, District> loadDistrictMap() {
        return districtRepository.findAll().stream()
                .collect(Collectors.toMap(District::getCode, d -> d));
    }

    private NeighborhoodMaps loadNeighborhoodMaps() {
        List<Neighborhood> all = neighborhoodRepository.findAll();

        Map<String, Neighborhood> neighborhoodCodeMap = all.stream()
                .collect(Collectors.toMap(Neighborhood::getCode, n -> n));

        Map<String, Neighborhood> neighborhoodKeyMap = all.stream()
                .collect(Collectors.toMap(
                        n -> n.getDistrict().getId() + "|" + n.getName(),
                        n -> n));

        return NeighborhoodMaps.builder()
                .neighborhoodCodeMap(neighborhoodCodeMap)
                .neighborhoodKeyMap(neighborhoodKeyMap).build();
    }

    private Map<String, Ri> loadRiMap() {
        return riRepository.findAll().stream()
                .collect(Collectors.toMap(Ri::getCode, ri -> ri));
    }


    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String neighborhoodKey(District district, String name) {
        return district.getId() + "|" + name;
    }

    @Data
    @Builder
    public static class NeighborhoodMaps {

        private Map<String, Neighborhood> neighborhoodCodeMap;

        private Map<String, Neighborhood> neighborhoodKeyMap;

    }

}
