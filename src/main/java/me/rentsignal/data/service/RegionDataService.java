package me.rentsignal.data.service;

import lombok.RequiredArgsConstructor;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
import me.rentsignal.location.entity.District;
import me.rentsignal.location.entity.Province;
import me.rentsignal.location.entity.Region;
import me.rentsignal.location.repository.DistrictRepository;
import me.rentsignal.location.repository.ProvinceRepository;
import me.rentsignal.location.repository.RegionRepository;
import me.rentsignal.user.entity.Role;
import me.rentsignal.user.service.AuthService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionDataService {

    private final AuthService authService;
    private final DistrictRepository districtRepository;
    private final RegionRepository regionRepository;
    private final ProvinceRepository provinceRepository;

    @Transactional
    public void saveRegion(Long userId) {
        authService.validateUserAccess(userId, Role.ROLE_ADMIN);

        Province province = provinceRepository.findByName("서울특별시").orElseThrow(() ->
                new BaseException(ErrorCode.PROVINCE_NOT_FOUND, "해당 시/도가 존재하지 않습니다. - 서울특별시"));

        // 서울 > 강북지역 > 도심권
        Region northCentral = regionRepository.save(
                Region.builder().areaGroup("강북")
                .areaName("도심권").build());
        northCentral.addDistricts(List.of(findDistrictByNameAndProvince("종로구", province),
                findDistrictByNameAndProvince("중구", province), findDistrictByNameAndProvince("용산구", province)));

        // 서울 > 강북지역 > 동북권
        Region northNorthEast = regionRepository.save(
                Region.builder().areaGroup("강북")
                .areaName("동북권").build());
        northNorthEast.addDistricts(List.of(findDistrictByNameAndProvince("도봉구", province),
                findDistrictByNameAndProvince("강북구", province), findDistrictByNameAndProvince("노원구", province),
                findDistrictByNameAndProvince("성북구", province), findDistrictByNameAndProvince("중랑구", province),
                findDistrictByNameAndProvince("성동구", province), findDistrictByNameAndProvince("광진구", province)));

        // 서울 > 강북지역 > 서북권
        Region northNorthWest = regionRepository.save(
                Region.builder().areaGroup("강북")
                .areaName("서북권").build());
        northNorthWest.addDistricts(List.of(findDistrictByNameAndProvince("은평구", province),
                findDistrictByNameAndProvince("서대문구", province), findDistrictByNameAndProvince("마포구", province)));

        // 서울 > 강남지역 > 서남권
        Region southSouthWest = regionRepository.save(
                Region.builder().areaGroup("강남")
                .areaName("서남권").build());
        southSouthWest.addDistricts(List.of(findDistrictByNameAndProvince("영등포구", province),
                findDistrictByNameAndProvince("동작구", province), findDistrictByNameAndProvince("관악구", province),
                findDistrictByNameAndProvince("금천구", province), findDistrictByNameAndProvince("강서구", province),
                findDistrictByNameAndProvince("양천구", province), findDistrictByNameAndProvince("구로구", province)));

        // 서울 > 강남지역 > 동남권
        Region southSouthEast = regionRepository.save(
                Region.builder().areaGroup("강남")
                .areaName("동남권").build());
        southSouthEast.addDistricts(List.of(findDistrictByNameAndProvince("서초구", province),
                findDistrictByNameAndProvince("강남구", province), findDistrictByNameAndProvince("송파구", province),
                findDistrictByNameAndProvince("강동구", province)));
    }

    private District findDistrictByNameAndProvince(String name, Province province) {
        return districtRepository.findByNameAndProvince(name, province).orElseThrow(() ->
                new BaseException(ErrorCode.DISTRICT_NOT_FOUND, "해당 시/군/구를 찾을 수 없습니다. - " + province.getName() + " " + name)
        );
    }

}
