package me.rentsignal.data.reader;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import me.rentsignal.data.dto.SubwayCsvRowDto;
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
public class SubwayCsvReader {

    /** csv 파일의 행 -> SubwayCsvRowDto 변환 */
    public List<SubwayCsvRowDto> read() {
        List<SubwayCsvRowDto> rows = new ArrayList<>();

        try (
                InputStreamReader isr = new InputStreamReader(
                        new ClassPathResource("subway.csv").getInputStream(), StandardCharsets.UTF_8
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

                String name = lines[1];
                String line = lines[2];
                String latitudeText = lines[3] == null ? "" : lines[3].trim();
                String longitudeText = lines[4] == null ? "" : lines[4].trim();

                if (latitudeText.isBlank() || longitudeText.isBlank()) {
                    log.warn("위도/경도 누락 - name={} lat={}, lng={}", lines[1], lines[3], lines[4]);
                    continue;
                }

                Double latitude = Double.parseDouble(latitudeText);
                Double longitude = Double.parseDouble(longitudeText);

                rows.add(
                        SubwayCsvRowDto.builder()
                                .name(name)
                                .line(line)
                                .latitude(latitude)
                                .longitude(longitude).build());
            }
        } catch (IOException | CsvValidationException e) {
            log.error("데이터 로드 실패" + e.getMessage());
            throw new BaseException(ErrorCode.CSV_READ_ERROR, "지하철 데이터 csv 파일 읽기에 실패했습니다. - " + e.getMessage());
        }

        return rows;
    }

}
