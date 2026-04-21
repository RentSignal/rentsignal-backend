package me.rentsignal.data.service;

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

    /** csv 파일 읽어서 법정동 데이터 (코드, 시/도, 시/군/구, 읍/면/동, 리) 저장 */
    public void importLegalDongCsv() {
        // 1. 편의를 위해 csv 한 행 -> LegalDongCsvRowDto로 변환
        List<LegalDongCsvRowDto> rows = legalDongCsvReader.read();

        // 2. DB 조회 최소화를 위해 행정구역 레벨별 데이터를 code 기준으로 map 생성
        Map<String, Province> provinceMap = loadProvinceMap();
        Map<String, District> districtMap = loadDistrictMap();
        Map<String, Neighborhood> neighborhoodMap = loadNeighborhoodMap();
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
                saveNeighborhood(row, provinceMap, districtMap, neighborhoodMap);
            }
            else if (isRiRow(row)) { // 리 레벨
                saveRi(row, provinceMap, districtMap, neighborhoodMap, riMap);
            }
        }
    }

    // ---------- 행정구역 레벨별로 저장 ----------

    private void saveProvince(LegalDongCsvRowDto row, Map<String, Province> map) {
        // 이미 해당 code의 Province가 존재할 경우 저장 필요 X
        Province province = map.get(row.getCode());
        if (province != null) return;

        Province newProvince = provinceRepository.save(Province.builder()
                .name(row.getProvinceName())
                .code(row.getCode()).build());

        map.put(newProvince.getCode(), newProvince);
    }

    private void saveDistrict(LegalDongCsvRowDto row, Map<String, Province> provinceMap, Map<String, District> districtMap) {
        District district = districtMap.get(row.getCode());
        if (district != null) return;

        Province province = findProvinceByName(provinceMap, row.getProvinceName());

        District newDistrict = districtRepository.save(District.builder()
                .name(row.getDistrictName())
                .code(row.getCode())
                .province(province).build());

        districtMap.put(newDistrict.getCode(), newDistrict);
    }

    private void saveNeighborhood(LegalDongCsvRowDto row,
                                          Map<String, Province> provinceMap,
                                          Map<String, District> districtMap,
                                          Map<String, Neighborhood> neighborhoodMap) {
        Neighborhood neighborhood = neighborhoodMap.get(row.getCode());
        if (neighborhood != null) return;

        Province province = findProvinceByName(provinceMap, row.getProvinceName());
        District district = findDistrictByNameAndProvince(districtMap, province, row.getDistrictName());

        Neighborhood newNeighborhood = neighborhoodRepository.save(Neighborhood.builder()
                .name(row.getNeighborhoodName())
                .code(row.getCode())
                .district(district).build());

        neighborhoodMap.put(newNeighborhood.getCode(), newNeighborhood);
    }

    private void saveRi(LegalDongCsvRowDto row,
                      Map<String, Province> provinceMap,
                      Map<String, District> districtMap,
                      Map<String, Neighborhood> neighborhoodMap,
                      Map<String, Ri> riMap) {
        Ri ri = riMap.get(row.getCode());
        if (ri != null) return;

        Province province = findProvinceByName(provinceMap, row.getProvinceName());
        District district = findDistrictByNameAndProvince(districtMap, province, row.getDistrictName());
        Neighborhood neighborhood = findNeighborhoodByNameAndDistrict(neighborhoodMap, district, row.getNeighborhoodName());

        Ri newRi = riRepository.save(Ri.builder()
                .name(row.getRiName())
                .code(row.getCode())
                .neighborhood(neighborhood).build());

        riMap.put(newRi.getCode(), newRi);
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

    private Neighborhood findNeighborhoodByNameAndDistrict(Map<String, Neighborhood> neighborhoodMap, District district, String name) {
        return neighborhoodMap.values().stream()
                .filter(n -> n.getDistrict().getId().equals(district.getId()))
                .filter(n -> n.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new BaseException(ErrorCode.NEIGHBORHOOD_NOT_FOUND, "해당 읍/면/동을 찾을 수 없습니다. - " + district.getName() + " " + name));
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

    private Map<String, Neighborhood> loadNeighborhoodMap() {
        return neighborhoodRepository.findAll().stream()
                .collect(Collectors.toMap(Neighborhood::getCode, n -> n));
    }

    private Map<String, Ri> loadRiMap() {
        return riRepository.findAll().stream()
                .collect(Collectors.toMap(Ri::getCode, ri -> ri));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

}
