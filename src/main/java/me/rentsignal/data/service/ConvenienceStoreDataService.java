package me.rentsignal.data.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rentsignal.data.dto.ConvenienceStoreApiResponseDto;
import me.rentsignal.data.external.ExternalApiClient;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
import me.rentsignal.location.entity.District;
import me.rentsignal.location.entity.Neighborhood;
import me.rentsignal.location.entity.Province;
import me.rentsignal.location.repository.DistrictRepository;
import me.rentsignal.location.repository.NeighborhoodRepository;
import me.rentsignal.location.repository.ProvinceRepository;
import me.rentsignal.locationInfo.entity.NeighborhoodConvenience;
import me.rentsignal.locationInfo.repository.NeighborhoodConvenienceRepository;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConvenienceStoreDataService {

    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final NeighborhoodRepository neighborhoodRepository;
    private final NeighborhoodConvenienceRepository neighborhoodConvenienceRepository;
    private final ExternalApiClient externalApiClient;

    private static final GeometryFactory geometryFactory = new GeometryFactory();
    private static final MathTransform transform = createTransfrom();

    @Value("${CONVENIENCE_STORE_API_URL}")
    private String CONVENIENCE_STORE_API_URL;

    /** 편의시설 - 편의점 저장 */
    @Transactional
    public void saveConvenienceStore() {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Province> provinceMap = loadProvinceNameMap();
        Map<String, District> districtMap = loadDistrictKeyMap();
        Map<String, Neighborhood> neighborhoodMap = loadNeighborhoodKeyMap();

        Set<String> convenienceStoreKeySet = neighborhoodConvenienceRepository.findAll().stream()
                .map(c -> convenienceStoreKey(c.getName(), c.getNeighborhood()))
                .collect(Collectors.toSet());

        // 편의점 데이터 총 54342개, 한 페이지 최대 데이터 개수 1000개 -> 55번 반복
        for (int i = 0; i < 55; i++) {
            System.out.println((i+1) + "번째 페이지 조회 중 ..");

            // API에 요청
            ConvenienceStoreApiResponseDto convenienceStoreApiResponseDto =
                    externalApiClient.getResponse(CONVENIENCE_STORE_API_URL + (i + 1), ConvenienceStoreApiResponseDto.class);

            List<ConvenienceStoreApiResponseDto.Item> stores = convenienceStoreApiResponseDto.getBody().getItems().getItem();

            // 페이지 내 모든 편의점 저장
            for (ConvenienceStoreApiResponseDto.Item item : stores) {
                String address = item.getAddress();

                if (address == null || address.isBlank()) {
                    log.warn("address가 null이거나 비어있습니다. - " + item.getFacilityName());
                    continue;
                }

                String[] arr = address.trim().split("\\s+");

                if (arr.length < 3) {
                    log.warn("주소 배열 길이가 3보다 작습니다. - " + address);
                    continue;
                }

                // 주소에 해당하는 Neighborhood 찾기
                Province province = findProvinceByName(provinceMap, arr[0]);
                District district;
                Neighborhood neighborhood;

                String convertedName = convertDistrictName(arr[1], arr[2]);

                if (convertedName == null) { // XX시 OO구 형태 X
                    if (province.getName().equals("세종특별자치시") && arr[1].equals("세종특별자치시")){
                        // 세종특별자치시 세종특별자치시 -> 세종특별자치시 세종시
                        arr[1] = "세종시";
                    }
                    district = findDistrictByNameAndProvince(districtMap, province, arr[1]);
                    neighborhood = findNeighborhoodByNameAndDistrict(neighborhoodMap, district, arr[2]);
                } else {
                    if (arr.length < 4) {
                        log.warn("잘못된 주소 형식입니다. - " + address);
                        continue;
                    }
                    district = findDistrictByNameAndProvince(districtMap, province, convertedName);
                    neighborhood = findNeighborhoodByNameAndDistrict(neighborhoodMap, district, arr[3]);
                }

                // 동일 neighborhood의 동일 이름 가게인지 확인
                String storeName = getNormalizedStoreName(item.getFacilityName());

                String key = convenienceStoreKey(storeName, neighborhood);
                if (convenienceStoreKeySet.contains(key)) {
                    log.info("이미 저장된 편의점입니다. - " + item.getFacilityName());
                    continue;
                }

                // Web Mercator 좌표 (x,y)를 WGS84 위도/경도로 변환
                if (item.getX() == null || item.getY() == null || item.getX() == 0 || item.getY() == 0) {
                    log.warn("x, y 값이 null이거나 0입니다. x - { }, y - {}", item.getX(), item.getY());
                    continue;
                }

                List<Double> latLng = convertToLatLng(item.getX(), item.getY());

                // 편의점 저장
                neighborhoodConvenienceRepository.save(NeighborhoodConvenience.builder()
                        .name(storeName)
                        .type("편의점")
                        .neighborhood(neighborhood)
                        .latitude(latLng.get(0))
                        .longitude(latLng.get(1)).build());

                convenienceStoreKeySet.add(key);
            }
        }
    }

    /** 전각/반각을 같은 이름으로 판단해서 Duplicate Entry 오류 발생
     * -> 정규화된 이름으로 변환 */
    private String getNormalizedStoreName(String name) {
        if (name == null || name.isBlank()) return null;

        return Normalizer.normalize(name.trim(), Normalizer.Form.NFKC);
    }

    /** XX시 OO구를 XX시OO구라는 하나의 District 이름으로 변환 */
    private String convertDistrictName(String name1, String name2) {
        for (String city : SubwayIndexService.CITIES) {
            if (name1.startsWith(city) && name2.endsWith("구"))
                return name1 + name2;
        }

        return null;
    }

    /** 동일 neighborhood 내 동일 이름의 가게 중복 막기 위한 key */
    private String convenienceStoreKey (String name, Neighborhood neighborhood) {
        return name.trim() + "|" + neighborhood.getId();
    }

    /** EPSG:3857 (x,y) -> EPSG:4326 (위도, 경도) 변환 */
    private List<Double> convertToLatLng(Double x, Double y) {
        try {
            Point sourcePoint = geometryFactory.createPoint(new Coordinate(x, y));
            Point targetPoint = (Point) JTS.transform(sourcePoint, transform);

            // EPSG:4326에서는 x = 경도, y = 위도
            return List.of(targetPoint.getY(), targetPoint.getX());
        } catch (Exception e) {
            log.error("좌표 변환 실패 - " + e);
            throw new BaseException(ErrorCode.CONVERT_FAILED, "위도/경도 변환에 실패했습니다.");
        }
    }

    private static MathTransform createTransfrom() {
        try {
            CoordinateReferenceSystem sourceCrs = CRS.decode("EPSG:3857", true);
            CoordinateReferenceSystem targetCrs = CRS.decode("EPSG:4326", true);
            return CRS.findMathTransform(sourceCrs, targetCrs);
        } catch (Exception e) {
            log.error("좌표 변환 실패 - " + e);
            throw new BaseException(ErrorCode.CONVERT_FAILED, "위도/경도 변환에 실패했습니다.");
        }
    }


    // ---------- 행정구역 레벨별로 저장된 데이터 전체 조회 후 이름/복합키 기준 Map으로 변환 ----------

    private Map<String, Province> loadProvinceNameMap() {
        return provinceRepository.findAll().stream()
                .collect(Collectors.toMap(Province::getName, p -> p));
    }

    private Map<String, District> loadDistrictKeyMap() {
        return districtRepository.findAll().stream()
                .collect(Collectors.toMap(
                        d -> districtKey(d.getProvince().getName(), d.getName()),
                        d -> d
                ));
    }

    private Map<String, Neighborhood> loadNeighborhoodKeyMap() {
        return neighborhoodRepository.findAll().stream()
                .collect(Collectors.toMap(
                        n -> neighborhoodKey(n.getDistrict().getProvince().getName(), n.getDistrict().getName(), n.getName()),
                        n -> n
                ));
    }


    // ---------- 행정구역 레벨별 map에서 이름으로 조회 ----------

    private Province findProvinceByName(Map<String, Province> provinceMap, String name) {
        Province province = provinceMap.get(name);

        if (province == null)
            throw new BaseException(ErrorCode.PROVINCE_NOT_FOUND, "해당 시/도를 찾을 수 없습니다. - " + name);

        return province;
    }

    private District findDistrictByNameAndProvince(Map<String, District> districtMap, Province province, String name) {
        String key = districtKey(province.getName(), name);
        District district = districtMap.get(key);

        if (district == null)
            throw new BaseException(ErrorCode.DISTRICT_NOT_FOUND, "해당 시/군/구를 찾을 수 없습니다. - " + province.getName() + " " + name);

        return district;
    }

    private Neighborhood findNeighborhoodByNameAndDistrict(Map<String, Neighborhood> neighborhoodMap, District district, String name) {
        String key = neighborhoodKey(district.getProvince().getName(), district.getName(), name);
        Neighborhood neighborhood = neighborhoodMap.get(key);

        if (neighborhood == null)
            throw new BaseException(ErrorCode.NEIGHBORHOOD_NOT_FOUND, "해당 읍/면/동을 찾을 수 없습니다. - " + district.getName() + " " + name);

        return neighborhood;
    }


    // ---------- 복합키 생성 ----------

    private String districtKey(String provinceName, String districtName) {
        return provinceName + "|" + districtName;
    }

    private String neighborhoodKey(String provinceName, String districtName, String neighborhoodName) {
        return provinceName + "|" + districtName + "|" + neighborhoodName;
    }

}
