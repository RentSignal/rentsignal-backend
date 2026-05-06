package me.rentsignal.data.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rentsignal.data.dto.ConvenienceRowDto;
import me.rentsignal.data.reader.ConvenienceCsvReader;
import me.rentsignal.location.entity.Neighborhood;
import me.rentsignal.locationInfo.entity.ConvenienceType;
import me.rentsignal.locationInfo.entity.NeighborhoodConvenience;
import me.rentsignal.locationInfo.repository.NeighborhoodConvenienceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConvenienceDataService {

    private final ConvenienceCsvReader convenienceCsvReader;
    private final LegalDongImportService legalDongImportService;
    private final NeighborhoodConvenienceRepository neighborhoodConvenienceRepository;

    /** 편의점 제외 편의시설 저장 */
    @Transactional
    public void saveConvenience() {
        List<ConvenienceRowDto> rows = convenienceCsvReader.read();
        Map<String, Neighborhood> neighborhoodMap = legalDongImportService.loadNeighborhoodMap();

        Set<String> convenienceKeySet = neighborhoodConvenienceRepository.findAll().stream()
                .map(c -> convenienceKey(c.getName(), c.getNeighborhood()))
                .collect(Collectors.toSet());

        int count = 0;

        for (ConvenienceRowDto row : rows) {
            String legalDongCode = row.getCode();

            Neighborhood neighborhood = neighborhoodMap.get(legalDongCode);
            if (neighborhood == null) {
                log.warn("해당 코드의 읍면동을 찾지 못했습니다. - {}", legalDongCode);
                continue;
            }

            String name = row.getName();
            String key = convenienceKey(name, neighborhood);
            if (convenienceKeySet.contains(key)) {
                continue;
            }

            // 업소 종류 코드를 "병원", "편의점", "카페", "마트" 문자열로 변환
            String typeCode = row.getType();
            ConvenienceType type = switch (typeCode) {
                case "HP8" -> ConvenienceType.HOSPITAL;
                case "CE7" -> ConvenienceType.CAFE;
                case "MT1" -> ConvenienceType.MART;
                default -> null;
            };

            if (type == null) continue;

            NeighborhoodConvenience neighborhoodConvenience = NeighborhoodConvenience.builder()
                    .neighborhood(neighborhood)
                    .type(type)
                    .name(name)
                    .latitude(row.getLatitude())
                    .longitude(row.getLongitude()).build();

            neighborhoodConvenienceRepository.save(neighborhoodConvenience);
            count++;
            convenienceKeySet.add(key);


            if (count % 50000 == 0) {
                log.info("편의시설 데이터 저장 진행 중 .. {}건 완료", count);
            }
        }
    }

    /** 동일 neighborhood 내 동일 이름의 편의시설 중복 막기 위한 key */
    public String convenienceKey(String name, Neighborhood neighborhood) {
        return name.trim() + "|" + neighborhood.getId();
    }

}
