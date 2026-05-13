package me.rentsignal.data.reader;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import me.rentsignal.data.dto.SafetyRowDto;
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
public class SafetyCsvReader {

    /** csv 파일의 행 -> SafetyRowDto 변환 */
    public List<SafetyRowDto> read(String fileName) {
        List<SafetyRowDto> rows = new ArrayList<>();

        try (
                InputStreamReader isr = new InputStreamReader(
                        new ClassPathResource(fileName).getInputStream(), StandardCharsets.UTF_8
                );

                CSVReader reader = new CSVReader(isr)
        ){
            String[] lines;
            int skipRows = 5;

            while ((lines = reader.readNext()) != null) {
                // csv 파일 맨 윗줄 헤더 스킵
                if (skipRows > 0) {
                    skipRows--;
                    continue;
                }

                String districtName = lines[1];
                String countText = lines[2];

                if (districtName.isBlank() || countText.isBlank()) {
                    log.warn("데이터 누락 - districtName={} count={}", districtName, countText);
                    continue;
                }

                rows.add(
                        SafetyRowDto.builder()
                                .districtName(districtName)
                                .count(Integer.parseInt(countText)).build());
            }
        } catch (IOException | CsvValidationException e) {
            log.error("데이터 로드 실패" + e.getMessage());
            throw new BaseException(ErrorCode.CSV_READ_ERROR, "CCTV 데이터 csv 또는 범죄 데이터 csv 파일 읽기에 실패했습니다. - " + e.getMessage());
        }

        return rows;
    }

}
