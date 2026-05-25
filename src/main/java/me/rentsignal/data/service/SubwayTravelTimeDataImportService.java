package me.rentsignal.data.service;

import lombok.RequiredArgsConstructor;
import me.rentsignal.data.dto.SubwayTravelTimeCsvRowDto;
import me.rentsignal.data.reader.SubwayTravelTimeCsvReader;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
import me.rentsignal.locationInfo.entity.NeighborhoodTransport;
import me.rentsignal.locationInfo.entity.SubwayTravelTime;
import me.rentsignal.locationInfo.entity.TransportType;
import me.rentsignal.locationInfo.repository.NeighborhoodTransportRepository;
import me.rentsignal.locationInfo.repository.SubwayTravelTimeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubwayTravelTimeDataImportService {

    private final SubwayTravelTimeCsvReader subwayTravelTimeCsvReader;
    private final NeighborhoodTransportRepository neighborhoodTransportRepository;
    private final SubwayTravelTimeRepository subwayTravelTimeRepository;

    @Transactional
    public void saveSubwayTravelTime() {
        List<SubwayTravelTimeCsvRowDto> rows = subwayTravelTimeCsvReader.read();

        Map<String, NeighborhoodTransport> neighborhoodTransportMap = loadNeighborhoodTransportMap();

        for (int i = 0; i < rows.size() - 1; i++) {
            SubwayTravelTimeCsvRowDto row = rows.get(i);
            SubwayTravelTimeCsvRowDto nextRow = rows.get(i + 1);

            String lineName = convertLineName(row);
            String transportName = row.getStationName() + "|" + lineName;

            String nextRowLineName = convertLineName(nextRow);
            String nextRowTransportName = nextRow.getStationName() + "|" + nextRowLineName;

            if (!lineName.equals(nextRowLineName)
                    && !lineName.equals("별내선") && !nextRowLineName.equals("8호선")
                    && !lineName.equals("1호선") && !nextRowLineName.equals("중앙선")) {
                // 환승 고려하지 않기 때문에 노선이 다르면 저장 X
                // 별내선 - 8호선 또는 1호선 - 중앙선일 경우만 저장
                continue;
            }

            NeighborhoodTransport transport = neighborhoodTransportMap.get(transportName);
            NeighborhoodTransport nextTransport = neighborhoodTransportMap.get(nextRowTransportName);

            if (transport == null || nextTransport == null) {
                String name = (transport == null) ? transportName : nextRowTransportName;
                throw new BaseException(ErrorCode.NEIGHBORHOOD_TRANSPORT_NOT_FOUND, "해당 neighborhood transport를 찾을 수 없습니다. - " + name);
            }

            NeighborhoodTransport fromTransport = transport;
            NeighborhoodTransport toTransport = nextTransport;

            // 중복되지 않도록 fromTransport에 더 작은 id를 가진 NeighborhoodTransport 지정
            if (fromTransport.getId() > toTransport.getId()) {
                fromTransport = nextTransport;
                toTransport = transport;
            }

            subwayTravelTimeRepository.save(
                    SubwayTravelTime.builder()
                            .lineName(lineName)
                            .fromStation(fromTransport)
                            .toStation(toTransport)
                            .travelTime(nextRow.getTravelTimeSeconds()).build());
        }
    }

    /** DB에 저장된 노선 이름으로 변환 */
    private String convertLineName(SubwayTravelTimeCsvRowDto row) {
        String rawLineName = row.getLineName();
        return rawLineName.matches("\\d+") // 숫자로만 이루어진 경우
                ?  rawLineName + "호선"
                : rawLineName + "선";
    }

    private Map<String, NeighborhoodTransport> loadNeighborhoodTransportMap() {
        return neighborhoodTransportRepository.findAllByTransportType(TransportType.SUBWAY_STATION).stream()
                .collect(Collectors.toMap(NeighborhoodTransport::getName, n -> n));
    }

}
