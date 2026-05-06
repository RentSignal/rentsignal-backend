package me.rentsignal.data.reader;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import me.rentsignal.data.dto.ConvenienceRowDto;
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
public class ConvenienceCsvReader {

    /** csv 파일의 행 -> ConvenienceRowDto 변환 */
    public List<ConvenienceRowDto> read() {
        List<ConvenienceRowDto> rows = new ArrayList<>();

        try (
                InputStreamReader isr = new InputStreamReader(
                        new ClassPathResource("convenience.csv").getInputStream(), StandardCharsets.UTF_8
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

                String legalDongCode = lines[0];
                String name = lines[2];
                String type = lines[3];
                String latitudeText = lines[6] == null ? "" : lines[6].trim();
                String longitudeText = lines[5] == null ? "" : lines[5].trim();

                if (legalDongCode.isBlank() || name.isBlank() || type.isBlank()
                || latitudeText.isBlank() || longitudeText.isBlank()) {
                    log.warn("데이터 누락 - legalDongCode={} name={}, type={}, lat={}, lng={}", legalDongCode, name, type, latitudeText, longitudeText);
                    continue;
                }

                Double latitude = Double.parseDouble(latitudeText);
                Double longitude = Double.parseDouble(longitudeText);

                rows.add(
                        ConvenienceRowDto.builder()
                                .code(legalDongCode)
                                .name(name)
                                .type(type)
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
