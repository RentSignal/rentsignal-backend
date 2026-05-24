package me.rentsignal.data.reader;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import me.rentsignal.data.dto.SubwayTravelTimeCsvRowDto;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class SubwayTravelTimeCsvReader {

    /** csv 파일의 행 -> SubwayTravelTimeCsvRowDto 변환 */
    public List<SubwayTravelTimeCsvRowDto> read() {
        List<SubwayTravelTimeCsvRowDto> rows = new ArrayList<>();

        try (
                InputStreamReader isr = new InputStreamReader(
                        new ClassPathResource("subway-travel-time.csv").getInputStream(), StandardCharsets.UTF_8
                );

                CSVReader reader = new CSVReader(isr)
        ){
            String[] lines;
            boolean isFirst = true;

            while ((lines = reader.readNext()) != null) {
                // csv 파일 맨 윗줄 헤더 스킵
                if (isFirst) {
                    isFirst = false;
                    continue;
                }

                String lineName = lines[0];
                String stationName = lines[1];
                String travelTimeSecondsText = lines[2];

                if (travelTimeSecondsText.isBlank()) {
                    log.warn("역간 소요시간 누락 - lineName={} stationName={}, travelTimeSeconds={}", lineName, stationName, travelTimeSecondsText);
                    continue;
                }

                int travelTimeSeconds = Integer.parseInt(travelTimeSecondsText);

                rows.add(
                        SubwayTravelTimeCsvRowDto.builder()
                                .lineName(lineName)
                                .stationName(stationName)
                                .travelTimeSeconds(travelTimeSeconds).build());
            }
        } catch (IOException | CsvValidationException e) {
            log.error("데이터 로드 실패" + e.getMessage());
            throw new BaseException(ErrorCode.CSV_READ_ERROR, "지하철 역간 소요시간 데이터 csv 파일 읽기에 실패했습니다. - " + e.getMessage());
        }

        return rows;
    }

}
