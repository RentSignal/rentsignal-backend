package me.rentsignal.data.service;

import lombok.RequiredArgsConstructor;
import me.rentsignal.data.dto.SubwayCsvRowDto;
import me.rentsignal.data.reader.SubwayCsvReader;
import me.rentsignal.locationInfo.entity.NeighborhoodTransport;
import me.rentsignal.locationInfo.entity.TransportType;
import me.rentsignal.locationInfo.repository.NeighborhoodTransportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubwayDataService {

    private final SubwayCsvReader subwayCsvReader;
    private final NeighborhoodTransportRepository neighborhoodTransportRepository;
    private final TransportNeighborhoodMappingService transportNeighborhoodMappingService;

    @Transactional
    public int importAndMapSubwayCsv() {
        List<SubwayCsvRowDto> rows = subwayCsvReader.read();

        for (SubwayCsvRowDto row : rows) {
            NeighborhoodTransport subwayStation = NeighborhoodTransport.builder()
                    .transportType(TransportType.SUBWAY_STATION)
                    .name(row.getName() + "|" + row.getLine()) // 역사명|호선꼴로 저장
                    .latitude(row.getLatitude())
                    .longitude(row.getLongitude()).build();

            neighborhoodTransportRepository.save(subwayStation);
        }

        // neighborhood에 매핑
        return transportNeighborhoodMappingService.mapTransportNeighborhood();
    }
}
