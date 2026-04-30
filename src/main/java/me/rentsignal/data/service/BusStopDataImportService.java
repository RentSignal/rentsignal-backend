package me.rentsignal.data.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rentsignal.data.reader.BusStopCsvReader;
import me.rentsignal.data.dto.BusStopCsvRowDto;
import me.rentsignal.locationInfo.entity.NeighborhoodTransport;
import me.rentsignal.locationInfo.entity.TransportType;
import me.rentsignal.locationInfo.repository.NeighborhoodTransportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusStopDataImportService {

    private final BusStopCsvReader busStopCsvReader;
    private final NeighborhoodTransportRepository neighborhoodTransportRepository;

    @Transactional
    public void importBusStopCsv() {
        List<BusStopCsvRowDto> rows = busStopCsvReader.read();

        int count = 0;

        for (BusStopCsvRowDto row : rows) {
            NeighborhoodTransport busStop = NeighborhoodTransport.builder()
                    .transportType(TransportType.BUS_STOP)
                    .name(row.getName())
                    .latitude(row.getLatitude())
                    .longitude(row.getLongitude()).build();

            neighborhoodTransportRepository.save(busStop);
            count++;

            if (count % 20000 == 0) {
                log.info("버스정류장 데이터 저장 진행 중 .. {}건 완료", count);
            }
        }
    }

}
