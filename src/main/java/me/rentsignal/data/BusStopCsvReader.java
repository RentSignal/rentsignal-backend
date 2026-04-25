package me.rentsignal.data;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import me.rentsignal.data.dto.BusStopCsvRowDto;
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
public class BusStopCsvReader {

    /** csv 파일의 행 -> BusStopCsvRowDto 변환 */
    public List<BusStopCsvRowDto> read() {
        List<BusStopCsvRowDto> rows = new ArrayList<>();

        try (
                InputStreamReader isr = new InputStreamReader(
                        new ClassPathResource("bus-stop.csv").getInputStream(), StandardCharsets.UTF_8
                );

                // 정류장명에 쉼표가 들어가는 행이 있어 쉼표로 구분 X -> OpenCSV 사용
                CSVReader reader = new CSVReader(isr)
        ){
            String[] line;
            Boolean isFirst = true;

            while ((line = reader.readNext()) != null) {
                // csv 파일 맨 윗줄 헤더 스킵
                if (isFirst) {
                    isFirst = false;
                    continue;
                }

                // 서울 데이터만 저장 시
//                String province = line[7] == null ? "" : line[7].trim();
//                if (!province.equals("서울특별시")) {
//                    continue;
//                }

                String name = line[1];
                String latitudeText = line[2] == null ? "" : line[2].trim();
                String longitudeText = line[3] == null ? "" : line[3].trim();

                if (latitudeText.isBlank() || longitudeText.isBlank()) {
                    log.warn("위도/경도 누락 - name={} lat={}, lng={}", line[1], line[2], line[3]);
                    continue;
                }

                Double latitude = Double.parseDouble(line[2]);
                Double longitude = Double.parseDouble(line[3]);

                rows.add(
                        BusStopCsvRowDto.builder()
                                .name(name)
                                .latitude(latitude)
                                .longitude(longitude).build());
            }
        } catch (IOException | CsvValidationException e) {
            log.error("데이터 로드 실패" + e.getMessage());
            throw new BaseException(ErrorCode.CSV_READ_ERROR, "전국 버스정류장 데이터 csv 파일 읽기에 실패했습니다. - " + e.getMessage());
        }

        return rows;
    }

}
